'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

exports.getLogFunction = getLogFunction;
exports.handleLabeledStatement = handleLabeledStatement;

exports.default = function (babel) {
  function _ref12(_id14) {
    if (!Plugin(_id14)) {
      throw new TypeError('Function return value violates contract.\n\nExpected:\nPlugin\n\nGot:\n' + _inspect(_id14));
    }

    return _id14;
  }

  if (!PluginParams(babel)) {
    throw new TypeError('Value of argument "babel" violates contract.\n\nExpected:\nPluginParams\n\nGot:\n' + _inspect(babel));
  }

  return _ref12({
    visitor: {
      Program: function Program(program, _ref15) {
        var opts = _ref15.opts;

        if (!NodePath(program)) {
          throw new TypeError('Value of argument "program" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(program));
        }

        program.traverse({
          LabeledStatement: function LabeledStatement(path) {
            if (!NodePath(path)) {
              throw new TypeError('Value of argument "path" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(path));
            }

            handleLabeledStatement(babel, path, opts);
          }
        });
      }
    }
  });
};

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Plugin = function () {
  function Plugin(input) {
    return input != null && Visitors(input.visitor);
  }

  ;
  Object.defineProperty(Plugin, Symbol.hasInstance, {
    value: function value(input) {
      return Plugin(input);
    }
  });
  return Plugin;
}();

var PluginTemplate = function () {
  function PluginTemplate(input) {
    return typeof input === 'function';
  }

  ;
  Object.defineProperty(PluginTemplate, Symbol.hasInstance, {
    value: function value(input) {
      return PluginTemplate(input);
    }
  });
  return PluginTemplate;
}();

var PluginParams = function () {
  function PluginParams(input) {
    return input != null && input.types instanceof Object && PluginTemplate(input.template);
  }

  ;
  Object.defineProperty(PluginParams, Symbol.hasInstance, {
    value: function value(input) {
      return PluginParams(input);
    }
  });
  return PluginParams;
}();

var PluginOptions = function () {
  function PluginOptions(input) {
    return input != null && (input.aliases === undefined || input.aliases != null && _typeof(input.aliases) === 'object' && Object.keys(input.aliases).every(function (key) {
      var _key = input.aliases[key];
      return typeof _key === 'string' || Template(_key);
    })) && (input.strip === undefined || typeof input.strip === 'boolean' || input.strip != null && _typeof(input.strip) === 'object' && Object.keys(input.strip).every(function (key) {
      var _key2 = input.strip[key];
      return typeof _key2 === 'boolean' || _key2 != null && (typeof _key2 === 'undefined' ? 'undefined' : _typeof(_key2)) === 'object' && Object.keys(_key2).every(function (key) {
        var _key3 = _key2[key];
        return typeof _key3 === 'boolean';
      });
    }));
  }

  ;
  Object.defineProperty(PluginOptions, Symbol.hasInstance, {
    value: function value(input) {
      return PluginOptions(input);
    }
  });
  return PluginOptions;
}();

var LogFunction = function () {
  function LogFunction(input) {
    return typeof input === 'function';
  }

  ;
  Object.defineProperty(LogFunction, Symbol.hasInstance, {
    value: function value(input) {
      return LogFunction(input);
    }
  });
  return LogFunction;
}();

var LogLevel = function () {
  function LogLevel(input) {
    return input === 'trace0' || input === 'trace' || input === 'debug' || input === 'info' || input === 'warn' || input === 'error';
  }

  ;
  Object.defineProperty(LogLevel, Symbol.hasInstance, {
    value: function value(input) {
      return LogLevel(input);
    }
  });
  return LogLevel;
}();

var Visitors = function () {
  function Visitors(input) {
    return input != null && (typeof input === 'undefined' ? 'undefined' : _typeof(input)) === 'object' && Object.keys(input).every(function (key) {
      var _key4 = input[key];
      return Visitor(_key4);
    });
  }

  ;
  Object.defineProperty(Visitors, Symbol.hasInstance, {
    value: function value(input) {
      return Visitors(input);
    }
  });
  return Visitors;
}();

var Template = function () {
  function Template(input) {
    return typeof input === 'function';
  }

  ;
  Object.defineProperty(Template, Symbol.hasInstance, {
    value: function value(input) {
      return Template(input);
    }
  });
  return Template;
}();

var TemplateIds = function () {
  function TemplateIds(input) {
    return input != null && (typeof input === 'undefined' ? 'undefined' : _typeof(input)) === 'object' && Object.keys(input).every(function (key) {
      var _key5 = input[key];
      return Node(_key5);
    });
  }

  ;
  Object.defineProperty(TemplateIds, Symbol.hasInstance, {
    value: function value(input) {
      return TemplateIds(input);
    }
  });
  return TemplateIds;
}();

var Visitor = function () {
  function Visitor(input) {
    return typeof input === 'function';
  }

  ;
  Object.defineProperty(Visitor, Symbol.hasInstance, {
    value: function value(input) {
      return Visitor(input);
    }
  });
  return Visitor;
}();

var Node = function () {
  function Node(input) {
    return input != null && typeof input.type === 'string' && (input.node === undefined || input.node == null);
  }

  ;
  Object.defineProperty(Node, Symbol.hasInstance, {
    value: function value(input) {
      return Node(input);
    }
  });
  return Node;
}();

var Literal = function () {
  function Literal(input) {
    return input != null && (input.type === 'StringLiteral' || input.type === 'BooleanLiteral' || input.type === 'NumericLiteral' || input.type === 'NullLiteral' || input.type === 'RegExpLiteral');
  }

  ;
  Object.defineProperty(Literal, Symbol.hasInstance, {
    value: function value(input) {
      return Literal(input);
    }
  });
  return Literal;
}();

var Identifier = function () {
  function Identifier(input) {
    return input != null && typeof input.type === 'string' && typeof input.name === 'string';
  }

  ;
  Object.defineProperty(Identifier, Symbol.hasInstance, {
    value: function value(input) {
      return Identifier(input);
    }
  });
  return Identifier;
}();

var Scope = function () {
  function Scope(input) {
    return input != null && (typeof input === 'undefined' ? 'undefined' : _typeof(input)) === 'object';
  }

  ;
  Object.defineProperty(Scope, Symbol.hasInstance, {
    value: function value(input) {
      return Scope(input);
    }
  });
  return Scope;
}();

var NodePath = function () {
  function NodePath(input) {
    return input != null && typeof input.type === 'string' && Node(input.node) && Scope(input.scope);
  }

  ;
  Object.defineProperty(NodePath, Symbol.hasInstance, {
    value: function value(input) {
      return NodePath(input);
    }
  });
  return NodePath;
}();

var Metadata = function () {
  function Metadata(input) {
    return input != null && typeof input.indent === 'number' && typeof input.prefix === 'string' && typeof input.parentName === 'string' && typeof input.filename === 'string' && typeof input.dirname === 'string' && typeof input.basename === 'string' && typeof input.extname === 'string' && typeof input.hasStartMessage === 'boolean' && typeof input.isStartMessage === 'boolean';
  }

  ;
  Object.defineProperty(Metadata, Symbol.hasInstance, {
    value: function value(input) {
      return Metadata(input);
    }
  });
  return Metadata;
}();

var Message = function () {
  function Message(input) {
    return input != null && Literal(input.prefix) && Literal(input.indent) && Literal(input.parentName) && Literal(input.filename) && Literal(input.dirname) && Literal(input.basename) && Literal(input.extname) && Node(input.content);
  }

  ;
  Object.defineProperty(Message, Symbol.hasInstance, {
    value: function value(input) {
      return Message(input);
    }
  });
  return Message;
}();

var $handled = Symbol('handled');
var $normalized = Symbol('normalized');

var DEFAULT_LEVELS = {
  'trace0': true,
  'trace': false,
  'debug': false,
  'info': false,
  'warn': false,
  'error': false
};

var PRESERVE_CONTEXTS = normalizeEnv(process.env.TRACE_CONTEXT);
var PRESERVE_FILES = normalizeEnv(process.env.TRACE_FILE);
var PRESERVE_LEVELS = normalizeEnv(process.env.TRACE_LEVEL);

/**
 * Normalize an environment variable, used to override plugin options.
 */
function normalizeEnv(input) {
  function _ref(_id) {
    if (!(Array.isArray(_id) && _id.every(function (item) {
      return typeof item === 'string';
    }))) {
      throw new TypeError('Function "normalizeEnv" return value violates contract.\n\nExpected:\nstring[]\n\nGot:\n' + _inspect(_id));
    }

    return _id;
  }

  if (!(input == null || typeof input === 'string')) {
    throw new TypeError('Value of argument "input" violates contract.\n\nExpected:\n?string\n\nGot:\n' + _inspect(input));
  }

  if (!input) {
    return _ref([]);
  }
  return _ref(input.split(',').map(function (context) {
    return context.toLowerCase().trim();
  }).filter(function (id) {
    return id;
  }));
}

/**
 * Like `template()` but returns an expression, not an expression statement.
 */
function expression(input, template) {
  function _ref2(_id2) {
    if (!Template(_id2)) {
      throw new TypeError('Function "expression" return value violates contract.\n\nExpected:\nTemplate\n\nGot:\n' + _inspect(_id2));
    }

    return _id2;
  }

  if (!(typeof input === 'string')) {
    throw new TypeError('Value of argument "input" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(input));
  }

  if (!PluginTemplate(template)) {
    throw new TypeError('Value of argument "template" violates contract.\n\nExpected:\nPluginTemplate\n\nGot:\n' + _inspect(template));
  }

  var fn = template(input);

  if (!Template(fn)) {
    throw new TypeError('Value of variable "fn" violates contract.\n\nExpected:\nTemplate\n\nGot:\n' + _inspect(fn));
  }

  return _ref2(function (ids) {
    function _ref3(_id3) {
      if (!Node(_id3)) {
        throw new TypeError('Function return value violates contract.\n\nExpected:\nNode\n\nGot:\n' + _inspect(_id3));
      }

      return _id3;
    }

    if (!TemplateIds(ids)) {
      throw new TypeError('Value of argument "ids" violates contract.\n\nExpected:\nTemplateIds\n\nGot:\n' + _inspect(ids));
    }

    var node = fn(ids);

    if (!Node(node)) {
      throw new TypeError('Value of variable "node" violates contract.\n\nExpected:\nNode\n\nGot:\n' + _inspect(node));
    }

    return _ref3(node.expression ? node.expression : node);
  });
}

/**
 * The default log() function.
 */
function getLogFunction(_ref13, logLevel) {
  var t = _ref13.types,
      template = _ref13.template;

  function _ref4(_id4) {
    if (!LogFunction(_id4)) {
      throw new TypeError('Function "getLogFunction" return value violates contract.\n\nExpected:\nLogFunction\n\nGot:\n' + _inspect(_id4));
    }

    return _id4;
  }

  if (!PluginParams(arguments[0])) {
    throw new TypeError('Value of argument 0 violates contract.\n\nExpected:\nPluginParams\n\nGot:\n' + _inspect(arguments[0]));
  }

  if (!LogLevel(logLevel)) {
    throw new TypeError('Value of argument "logLevel" violates contract.\n\nExpected:\nLogLevel\n\nGot:\n' + _inspect(logLevel));
  }

  return _ref4(function log(message, metadata) {
    function _ref5(_id5) {
      if (!Node(_id5)) {
        throw new TypeError('Function "log" return value violates contract.\n\nExpected:\nNode\n\nGot:\n' + _inspect(_id5));
      }

      return _id5;
    }

    if (!Message(message)) {
      throw new TypeError('Value of argument "message" violates contract.\n\nExpected:\nMessage\n\nGot:\n' + _inspect(message));
    }

    if (!Metadata(metadata)) {
      throw new TypeError('Value of argument "metadata" violates contract.\n\nExpected:\nMetadata\n\nGot:\n' + _inspect(metadata));
    }

    if (t.isSequenceExpression(message.content)) {
      return _ref5(t.callExpression(t.memberExpression(t.memberExpression(t.identifier('logger'), t.identifier(logLevel)), t.identifier('apply')), [t.memberExpression(t.identifier('logger'), t.identifier(logLevel)), t.callExpression(t.memberExpression(t.identifier('logger'), t.identifier('params')), [t.stringLiteral(logLevel), t.identifier('this'), t.stringLiteral(metadata.context[0]), t.stringLiteral(metadata.context[1])].concat(message.content.expressions))]));
    } else {
      return _ref5(expression('logger[LEVEL].apply(logger[LEVEL], logger.params(LEVEL, SELF, FILE, CONTEXT, CONTENT))', template)({
        LEVEL: t.stringLiteral(logLevel),
        SELF: t.identifier('this'),
        FILE: t.stringLiteral(metadata.context[0]),
        CONTEXT: t.stringLiteral(metadata.context[1]),
        CONTENT: message.content
      }));
    }
  });
}

/**
 * Normalize the plugin options.
 */
function normalizeOpts(babel, opts) {
  function _ref6(_id6) {
    if (!PluginOptions(_id6)) {
      throw new TypeError('Function "normalizeOpts" return value violates contract.\n\nExpected:\nPluginOptions\n\nGot:\n' + _inspect(_id6));
    }

    return _id6;
  }

  if (!PluginParams(babel)) {
    throw new TypeError('Value of argument "babel" violates contract.\n\nExpected:\nPluginParams\n\nGot:\n' + _inspect(babel));
  }

  if (!PluginOptions(opts)) {
    throw new TypeError('Value of argument "opts" violates contract.\n\nExpected:\nPluginOptions\n\nGot:\n' + _inspect(opts));
  }

  if (opts[$normalized]) {
    return _ref6(opts);
  }
  if (!opts.aliases) {
    opts.aliases = {};

    Object.keys(DEFAULT_LEVELS).forEach(function (a) {
      opts.aliases[a] = getLogFunction(babel, a);
    });
  } else {
    Object.keys(opts.aliases).forEach(function (key) {
      if (typeof opts.aliases[key] === 'string' && opts.aliases[key]) {
        var expr = expression(opts.aliases[key], babel.template);

        if (!(typeof expr === 'function')) {
          throw new TypeError('Value of variable "expr" violates contract.\n\nExpected:\n(Message) => Node\n\nGot:\n' + _inspect(expr));
        }

        opts.aliases[key] = function (message) {
          function _ref7(_id7) {
            if (!Node(_id7)) {
              throw new TypeError('Function return value violates contract.\n\nExpected:\nNode\n\nGot:\n' + _inspect(_id7));
            }

            return _id7;
          }

          if (!Message(message)) {
            throw new TypeError('Value of argument "message" violates contract.\n\nExpected:\nMessage\n\nGot:\n' + _inspect(message));
          }

          return _ref7(expr(message));
        };
      }
    });
  }
  if (opts.strip === undefined) {
    opts.strip = {};

    for (var level in DEFAULT_LEVELS) {
      opts.strip[level] = {
        production: DEFAULT_LEVELS[level],
        development: false
      };
    }
  }
  opts[$normalized] = true;
  return _ref6(opts);
}

function generatePrefix(dirname, basename) {
  function _ref8(_id8) {
    if (!(typeof _id8 === 'string')) {
      throw new TypeError('Function "generatePrefix" return value violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(_id8));
    }

    return _id8;
  }

  if (!(typeof dirname === 'string')) {
    throw new TypeError('Value of argument "dirname" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(dirname));
  }

  if (!(typeof basename === 'string')) {
    throw new TypeError('Value of argument "basename" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(basename));
  }

  if (basename !== 'index') {
    return basename;
  }
  basename = _path2.default.basename(dirname);
  if (basename !== 'src' && basename !== 'lib') {
    return basename;
  }
  return _ref8(_path2.default.basename(_path2.default.dirname(dirname)));
}

/**
 * Collect the metadata for a given node path, which will be
 * made available to logging functions.
 */
function collectMetadata(path, opts) {
  function _ref9(_id9) {
    if (!Metadata(_id9)) {
      throw new TypeError('Function "collectMetadata" return value violates contract.\n\nExpected:\nMetadata\n\nGot:\n' + _inspect(_id9));
    }

    return _id9;
  }

  if (!NodePath(path)) {
    throw new TypeError('Value of argument "path" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(path));
  }

  if (!PluginOptions(opts)) {
    throw new TypeError('Value of argument "opts" violates contract.\n\nExpected:\nPluginOptions\n\nGot:\n' + _inspect(opts));
  }

  var filename = _path2.default.resolve(process.cwd(), path.hub.file.opts.filename);

  if (!(typeof filename === 'string')) {
    throw new TypeError('Value of variable "filename" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(filename));
  }

  var dirname = _path2.default.dirname(filename);

  if (!(typeof dirname === 'string')) {
    throw new TypeError('Value of variable "dirname" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(dirname));
  }

  var extname = _path2.default.extname(filename);

  if (!(typeof extname === 'string')) {
    throw new TypeError('Value of variable "extname" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(extname));
  }

  var basename = _path2.default.basename(filename, extname);

  if (!(typeof basename === 'string')) {
    throw new TypeError('Value of variable "basename" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(basename));
  }

  var prefix = generatePrefix(dirname, basename);
  var names = [];

  if (!(Array.isArray(names) && names.every(function (item) {
    return typeof item === 'string';
  }))) {
    throw new TypeError('Value of variable "names" violates contract.\n\nExpected:\nstring[]\n\nGot:\n' + _inspect(names));
  }

  var indent = 0;
  var parent = void 0;

  if (!(parent == null || NodePath(parent))) {
    throw new TypeError('Value of variable "parent" violates contract.\n\nExpected:\n?NodePath\n\nGot:\n' + _inspect(parent));
  }

  var parentName = path.getAncestry().slice(1).reduce(function (parts, item) {
    if (!(Array.isArray(parts) && parts.every(function (item) {
      return typeof item === 'string';
    }))) {
      throw new TypeError('Value of argument "parts" violates contract.\n\nExpected:\nstring[]\n\nGot:\n' + _inspect(parts));
    }

    if (!NodePath(item)) {
      throw new TypeError('Value of argument "item" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(item));
    }

    if (item.isClassMethod()) {
      if (!parent) {
        parent = item;
      }
      parts.unshift(item.node.key.type === 'Identifier' ? item.node.key.name : '[computed method]');
    } else if (item.isClassDeclaration()) {
      if (!parent) {
        parent = item;
      }
      parts.unshift(item.node.id ? item.node.id.name : '[anonymous class@' + item.node.loc.start.line + ']');
    } else if (item.isFunction()) {
      if (!parent) {
        parent = item;
      }
      parts.unshift(item.node.id && item.node.id.name || '[anonymous@' + item.node.loc.start.line + ']');
    } else if (item.isProgram()) {
      if (!parent) {
        parent = item;
      }
    } else if (!parent && !item.isClassBody() && !item.isBlockStatement()) {
      indent++;
    }
    return parts;
  }, []).join(':');

  if (!(typeof parentName === 'string')) {
    throw new TypeError('Value of variable "parentName" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(parentName));
  }

  var hasStartMessage = false;
  var isStartMessage = false;
  if (parent && !parent.isProgram()) {
    _parent$get$get = parent.get('body').get('body');

    if (!(_parent$get$get && (typeof _parent$get$get[Symbol.iterator] === 'function' || Array.isArray(_parent$get$get)))) {
      throw new TypeError('Expected _parent$get$get to be iterable, got ' + _inspect(_parent$get$get));
    }

    var _iteratorNormalCompletion = true;
    var _didIteratorError = false;
    var _iteratorError = undefined;

    try {
      for (var _iterator = _parent$get$get[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
        var _parent$get$get;

        var _child = _step.value;

        if (!NodePath(_child)) {
          throw new TypeError('Value of variable "child" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(_child));
        }

        if (_child.node[$handled]) {
          hasStartMessage = true;
          break;
        }
        if (!_child.isLabeledStatement()) {
          break;
        }
        var label = _child.get('label');

        if (!NodePath(label)) {
          throw new TypeError('Value of variable "label" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(label));
        }

        if (opts.aliases[label.node.name]) {
          hasStartMessage = true;
          if (_child.node === path.node) {
            isStartMessage = true;
          }
          break;
        }
      }
    } catch (err) {
      _didIteratorError = true;
      _iteratorError = err;
    } finally {
      try {
        if (!_iteratorNormalCompletion && _iterator.return) {
          _iterator.return();
        }
      } finally {
        if (_didIteratorError) {
          throw _iteratorError;
        }
      }
    }
  }

  var context = [prefix, parentName];

  if (!(Array.isArray(context) && context.every(function (item) {
    return typeof item === 'string';
  }))) {
    throw new TypeError('Value of variable "context" violates contract.\n\nExpected:\nstring[]\n\nGot:\n' + _inspect(context));
  }

  return _ref9({ indent: indent, prefix: prefix, parentName: parentName, context: context, hasStartMessage: hasStartMessage, isStartMessage: isStartMessage, filename: filename, dirname: dirname, basename: basename, extname: extname });
}

/**
 * Determine whether the given logging statement should be stripped.
 */
function shouldStrip(name, metadata, _ref14) {
  var strip = _ref14.strip;

  if (!(typeof name === 'string')) {
    throw new TypeError('Value of argument "name" violates contract.\n\nExpected:\nstring\n\nGot:\n' + _inspect(name));
  }

  if (!Metadata(metadata)) {
    throw new TypeError('Value of argument "metadata" violates contract.\n\nExpected:\nMetadata\n\nGot:\n' + _inspect(metadata));
  }

  if (!PluginOptions(arguments[2])) {
    throw new TypeError('Value of argument 2 violates contract.\n\nExpected:\nPluginOptions\n\nGot:\n' + _inspect(arguments[2]));
  }

  switch (typeof strip === 'undefined' ? 'undefined' : _typeof(strip)) {
    case 'boolean':
      if (!strip) return false;
      // strip === true
      break;
    case 'object':
      var se = strip[name];
      if (!se || (typeof se === 'undefined' ? 'undefined' : _typeof(se)) === 'object' && !se[process.env.NODE_ENV]) return false;
      // strip[name] === true || strip[name][env] === true
      break;
    default:
      return false;
  }
  if (PRESERVE_CONTEXTS.length) {
    var context = metadata.context.toLowerCase();
    if (PRESERVE_CONTEXTS.some(function (pc) {
      return context.includes(pc);
    })) return false;
  }
  if (PRESERVE_FILES.length) {
    var _filename = metadata.filename.toLowerCase();
    if (PRESERVE_FILES.some(function (pf) {
      return _filename.includes(pf);
    })) return false;
  }
  if (PRESERVE_LEVELS.length) {
    var level = name.toLowerCase();
    if (PRESERVE_LEVELS.some(function (pl) {
      return level === pl;
    })) return false;
  }
  return true;
}

function handleLabeledStatement(babel, path, opts) {
  if (!PluginParams(babel)) {
    throw new TypeError('Value of argument "babel" violates contract.\n\nExpected:\nPluginParams\n\nGot:\n' + _inspect(babel));
  }

  if (!NodePath(path)) {
    throw new TypeError('Value of argument "path" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(path));
  }

  if (!PluginOptions(opts)) {
    throw new TypeError('Value of argument "opts" violates contract.\n\nExpected:\nPluginOptions\n\nGot:\n' + _inspect(opts));
  }

  var t = babel.types;
  var label = path.get('label');

  if (!NodePath(label)) {
    throw new TypeError('Value of variable "label" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(label));
  }

  opts = normalizeOpts(babel, opts);
  if (!opts.aliases[label.node.name]) {
    return;
  }

  var metadata = collectMetadata(path, opts);

  if (!Metadata(metadata)) {
    throw new TypeError('Value of variable "metadata" violates contract.\n\nExpected:\nMetadata\n\nGot:\n' + _inspect(metadata));
  }

  if (shouldStrip(label.node.name, metadata, opts)) {
    path.remove();
    return;
  }

  path.traverse({
    "VariableDeclaration|Function|AssignmentExpression|UpdateExpression|YieldExpression|ReturnStatement": function VariableDeclarationFunctionAssignmentExpressionUpdateExpressionYieldExpressionReturnStatement(item) {
      if (!NodePath(item)) {
        throw new TypeError('Value of argument "item" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(item));
      }

      throw path.buildCodeFrameError('Logging statements cannot have side effects.');
    },
    ExpressionStatement: function ExpressionStatement(statement) {
      if (!NodePath(statement)) {
        throw new TypeError('Value of argument "statement" violates contract.\n\nExpected:\nNodePath\n\nGot:\n' + _inspect(statement));
      }

      if (statement.node[$handled]) {
        return;
      }
      var message = {
        prefix: t.stringLiteral(metadata.prefix),
        content: statement.get('expression').node,
        hasStartMessage: t.booleanLiteral(metadata.hasStartMessage),
        isStartMessage: t.booleanLiteral(metadata.isStartMessage),
        indent: t.numericLiteral(metadata.indent),
        parentName: t.stringLiteral(metadata.parentName),
        filename: t.stringLiteral(metadata.filename),
        dirname: t.stringLiteral(metadata.dirname),
        basename: t.stringLiteral(metadata.basename),
        extname: t.stringLiteral(metadata.extname)
      };

      if (!Message(message)) {
        throw new TypeError('Value of variable "message" violates contract.\n\nExpected:\nMessage\n\nGot:\n' + _inspect(message));
      }

      var replacement = t.expressionStatement(opts.aliases[label.node.name](message, metadata));
      replacement[$handled] = true;
      statement.replaceWith(replacement);
    }
  });

  if (path.get('body').isBlockStatement()) {
    path.replaceWithMultiple(path.get('body').node.body);
  } else {
    path.replaceWith(path.get('body').node);
  }
}

/**
 * # Trace
 */

function _inspect(input, depth) {
  var maxDepth = 4;
  var maxKeys = 15;

  if (depth === undefined) {
    depth = 0;
  }

  depth += 1;

  if (input === null) {
    return 'null';
  } else if (input === undefined) {
    return 'void';
  } else if (typeof input === 'string' || typeof input === 'number' || typeof input === 'boolean') {
    return typeof input === 'undefined' ? 'undefined' : _typeof(input);
  } else if (Array.isArray(input)) {
    if (input.length > 0) {
      if (depth > maxDepth) return '[...]';

      var first = _inspect(input[0], depth);

      if (input.every(function (item) {
        return _inspect(item, depth) === first;
      })) {
        return first.trim() + '[]';
      } else {
        return '[' + input.slice(0, maxKeys).map(function (item) {
          return _inspect(item, depth);
        }).join(', ') + (input.length >= maxKeys ? ', ...' : '') + ']';
      }
    } else {
      return 'Array';
    }
  } else {
    var keys = Object.keys(input);

    if (!keys.length) {
      if (input.constructor && input.constructor.name && input.constructor.name !== 'Object') {
        return input.constructor.name;
      } else {
        return 'Object';
      }
    }

    if (depth > maxDepth) return '{...}';

    var _indent = '  '.repeat(depth - 1);

    var entries = keys.slice(0, maxKeys).map(function (key) {
      return (/^([A-Z_$][A-Z0-9_$]*)$/i.test(key) ? key : JSON.stringify(key)) + ': ' + _inspect(input[key], depth) + ';';
    }).join('\n  ' + _indent);

    if (keys.length >= maxKeys) {
      entries += '\n  ' + _indent + '...';
    }

    if (input.constructor && input.constructor.name && input.constructor.name !== 'Object') {
      return input.constructor.name + ' {\n  ' + _indent + entries + '\n' + _indent + '}';
    } else {
      return '{\n  ' + _indent + entries + '\n' + _indent + '}';
    }
  }
}
