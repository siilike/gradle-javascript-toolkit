<?php
header("Cache-Control: no-cache");

${'$version'} = @file_get_contents('/data1/sites/example/version/frontend-'.${'$_SERVER'}['ENV']);

if(empty(${'$version'}))
{
	ob_end_clean();
	header("HTTP/1.1 500 Internal Server Error");
	echo "Internal server error, please try again later.";
	exit(1);
}

${'$static'} = 'https://'.${'$_SERVER'}['STATIC_DOMAIN'].'/v'.${'$version'};

?><!DOCTYPE html>
<html>
<head>
	<title></title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="robots" content="index, follow" />
	<link rel="stylesheet" type="text/css" href="<?=${'$static'}?>/example.css" media="screen, projection, tv" />
	<meta name="viewport" content="width=device-width,initial-scale=1.0" />
	<meta http-equiv="X-UA-Compatible" content="IE-edge,chrome=1">
</head>
<body>

	<div id="loading-overlay">
		<div id="loading-progress"></div>
		<div id="loading-content">
			One moment, please...
			<p id="loaded"></p>
		</div>
	</div>

	<div id="root"></div>

	<script type="text/javascript">
		var scriptsBaseUrl = <?=json_encode(${'$static'}.'/')?>;
	</script>

	<script type="text/javascript">
		(function()
		{
			var progress = document.querySelector('#loading-progress');
			var loadedText = document.querySelector('#loaded');

			function updateProgress()
			{
				var r = getTotalReceived();
				var p = Math.round((r / total) * 1000) / 10;

				loadedText.innerText = Math.min(99, Math.round(p)) + "%";
				progress.style.width = p + '%';
			}

			var files = <% out.print groovy.json.JsonOutput.toJson([ libraries: libraries, modules: modules ]) %>;
			var total = 1;

			var preset = <% out.print groovy.json.JsonOutput.toJson(defaultPreset) %>;
			var presetRegexes = <% out.print groovy.json.JsonOutput.toJson(presetRegexes) %>;

			for(var name in presetRegexes)
			{
				if(new RegExp(presetRegexes[name]).test(navigator.userAgent))
				{
					preset = name;
					break;
				}
			}

			var scripts = [];
			var loaded = [];
			var received = [];

			for(var type in files)
			{
				for(var name in files[type])
				{
					for(var output in files[type][name])
					{
						var v = files[type][name][output][preset];

						if(type === 'libraries')
						{
							scripts.unshift(v.name);
						}
						else
						{
							scripts.push(v.name);
						}

						total += v.size;

						break;
					}
				}
			}

			function getTotalReceived()
			{
				var r = 0;

				for(var k = 0; k < received.length; k++)
				{
					if(received[k])
					{
						r += received[k];
					}
				}

				return r;
			}

			function load(i)
			{
				var xhr = new XMLHttpRequest();

				xhr.onload = function()
				{
					loaded[i] = xhr.responseText;

					for(var k = 0; k < scripts.length; k++)
					{
						if(!loaded[k])
						{
							return;
						}
					}

					var loadNext = function(i)
					{
						var s = document.createElement('script');
						s.setAttribute('src', scriptsBaseUrl + scripts[i]);

						if(i < scripts.length-1)
						{
							s.onload = function()
							{
								loadNext(i+1);
							};
						}

						document.body.appendChild(s);
					};

					loadNext(0);
					loaded = null;
				};

				xhr.onerror = function()
				{
					setTimeout(function()
					{
						load(i);
					}, 1000);
				};

				xhr.ontimeout = function()
				{
					load(i);
				};

				xhr.open('GET', scriptsBaseUrl + scripts[i], true);

				xhr.addEventListener("progress", function(e)
				{
					received[i] = e.loaded;

					updateProgress();
				}, false);

				xhr.send();
			};

			for(var i = 0; i < scripts.length; i++)
			{
				load(i);
			}
		})();
	</script>
</body>
</html>
