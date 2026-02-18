// Auto-generated - do not edit
import {createRequire} from 'module';
// node_modules/@anthropic-ai/claude-agent-sdk/sdk.mjs
import {dirname as QW, join as PV, join as oz, join as XW, join as GW} from "path";
import {fileURLToPath as gI} from "url";
import {setMaxListeners as qK} from "events";
import {spawn as Wq} from "child_process";
import {createInterface as Jq} from "readline";
import * as f from "fs";
import {appendFileSync as Qq, existsSync as Xq, mkdirSync as Yq, realpathSync as r7} from "fs";
import {mkdir as dV, open as nV, readdir as lV, readFile as JW, rename as iV, rm as pV, rmdir as cV, stat as uV, unlink as mV} from "fs/promises";
import {homedir as SV} from "os";
import {cwd as ZV} from "process";
import {randomUUID as CV, randomUUID as eV, randomUUID as Hq} from "crypto";
// main.mjs
import {createInterface} from "node:readline";

const require = createRequire(import.meta.url);

var GK = Object.create;
var { getPrototypeOf: HK, defineProperty: WQ, getOwnPropertyNames: BK } = Object;
var zK = Object.prototype.hasOwnProperty;
var q7 = (Q, X, Y) => {
  Y = Q != null ? GK(HK(Q)) : {};
  let $ = X || !Q || !Q.__esModule ? WQ(Y, "default", { value: Q, enumerable: true }) : Y;
  for (let W of BK(Q)) if (!zK.call($, W)) WQ($, W, { get: () => Q[W], enumerable: true });
  return $;
};
var P = (Q, X) => () => (X || Q((X = { exports: {} }).exports, X), X.exports);
var U7 = (Q, X) => {
  for (var Y in X) WQ(Q, Y, { get: X[Y], enumerable: true, configurable: true, set: ($) => X[Y] = () => $ });
};
var KK = Symbol.dispose || Symbol.for("Symbol.dispose");
var VK = Symbol.asyncDispose || Symbol.for("Symbol.asyncDispose");
var f4 = P((WG) => {
  Object.defineProperty(WG, "__esModule", { value: true });
  WG.regexpCode = WG.getEsmExportName = WG.getProperty = WG.safeStringify = WG.stringify = WG.strConcat = WG.addCodeArg = WG.str = WG._ = WG.nil = WG._Code = WG.Name = WG.IDENTIFIER = WG._CodeOrName = void 0;
  class w8 {
  }
  WG._CodeOrName = w8;
  WG.IDENTIFIER = /^[a-z$_][a-z$_0-9]*$/i;
  class f6 extends w8 {
    constructor(Q) {
      super();
      if (!WG.IDENTIFIER.test(Q)) throw Error("CodeGen: name must be a valid identifier");
      this.str = Q;
    }
    toString() {
      return this.str;
    }
    emptyStr() {
      return false;
    }
    get names() {
      return { [this.str]: 1 };
    }
  }
  WG.Name = f6;
  class a0 extends w8 {
    constructor(Q) {
      super();
      this._items = typeof Q === "string" ? [Q] : Q;
    }
    toString() {
      return this.str;
    }
    emptyStr() {
      if (this._items.length > 1) return false;
      let Q = this._items[0];
      return Q === "" || Q === '""';
    }
    get str() {
      var Q;
      return (Q = this._str) !== null && Q !== void 0 ? Q : this._str = this._items.reduce((X, Y) => `${X}${Y}`, "");
    }
    get names() {
      var Q;
      return (Q = this._names) !== null && Q !== void 0 ? Q : this._names = this._items.reduce((X, Y) => {
        if (Y instanceof f6) X[Y.str] = (X[Y.str] || 0) + 1;
        return X;
      }, {});
    }
  }
  WG._Code = a0;
  WG.nil = new a0("");
  function YG(Q, ...X) {
    let Y = [Q[0]], $ = 0;
    while ($ < X.length) Q$(Y, X[$]), Y.push(Q[++$]);
    return new a0(Y);
  }
  WG._ = YG;
  var eY = new a0("+");
  function $G(Q, ...X) {
    let Y = [h4(Q[0])], $ = 0;
    while ($ < X.length) Y.push(eY), Q$(Y, X[$]), Y.push(eY, h4(Q[++$]));
    return AN(Y), new a0(Y);
  }
  WG.str = $G;
  function Q$(Q, X) {
    if (X instanceof a0) Q.push(...X._items);
    else if (X instanceof f6) Q.push(X);
    else Q.push(IN(X));
  }
  WG.addCodeArg = Q$;
  function AN(Q) {
    let X = 1;
    while (X < Q.length - 1) {
      if (Q[X] === eY) {
        let Y = jN(Q[X - 1], Q[X + 1]);
        if (Y !== void 0) {
          Q.splice(X - 1, 3, Y);
          continue;
        }
        Q[X++] = "+";
      }
      X++;
    }
  }
  function jN(Q, X) {
    if (X === '""') return Q;
    if (Q === '""') return X;
    if (typeof Q == "string") {
      if (X instanceof f6 || Q[Q.length - 1] !== '"') return;
      if (typeof X != "string") return `${Q.slice(0, -1)}${X}"`;
      if (X[0] === '"') return Q.slice(0, -1) + X.slice(1);
      return;
    }
    if (typeof X == "string" && X[0] === '"' && !(Q instanceof f6)) return `"${Q}${X.slice(1)}`;
    return;
  }
  function RN(Q, X) {
    return X.emptyStr() ? Q : Q.emptyStr() ? X : $G`${Q}${X}`;
  }
  WG.strConcat = RN;
  function IN(Q) {
    return typeof Q == "number" || typeof Q == "boolean" || Q === null ? Q : h4(Array.isArray(Q) ? Q.join(",") : Q);
  }
  function EN(Q) {
    return new a0(h4(Q));
  }
  WG.stringify = EN;
  function h4(Q) {
    return JSON.stringify(Q).replace(/\u2028/g, "\\u2028").replace(/\u2029/g, "\\u2029");
  }
  WG.safeStringify = h4;
  function bN(Q) {
    return typeof Q == "string" && WG.IDENTIFIER.test(Q) ? new a0(`.${Q}`) : YG`[${Q}]`;
  }
  WG.getProperty = bN;
  function PN(Q) {
    if (typeof Q == "string" && WG.IDENTIFIER.test(Q)) return new a0(`${Q}`);
    throw Error(`CodeGen: invalid export name: ${Q}, use explicit $id name mapping`);
  }
  WG.getEsmExportName = PN;
  function SN(Q) {
    return new a0(Q.toString());
  }
  WG.regexpCode = SN;
});
var W$ = P((BG) => {
  Object.defineProperty(BG, "__esModule", { value: true });
  BG.ValueScope = BG.ValueScopeName = BG.Scope = BG.varKinds = BG.UsedValueState = void 0;
  var x0 = f4();
  class GG extends Error {
    constructor(Q) {
      super(`CodeGen: "code" for ${Q} not defined`);
      this.value = Q.value;
    }
  }
  var A8;
  (function(Q) {
    Q[Q.Started = 0] = "Started", Q[Q.Completed = 1] = "Completed";
  })(A8 || (BG.UsedValueState = A8 = {}));
  BG.varKinds = { const: new x0.Name("const"), let: new x0.Name("let"), var: new x0.Name("var") };
  class Y$ {
    constructor({ prefixes: Q, parent: X } = {}) {
      this._names = {}, this._prefixes = Q, this._parent = X;
    }
    toName(Q) {
      return Q instanceof x0.Name ? Q : this.name(Q);
    }
    name(Q) {
      return new x0.Name(this._newName(Q));
    }
    _newName(Q) {
      let X = this._names[Q] || this._nameGroup(Q);
      return `${Q}${X.index++}`;
    }
    _nameGroup(Q) {
      var X, Y;
      if (((Y = (X = this._parent) === null || X === void 0 ? void 0 : X._prefixes) === null || Y === void 0 ? void 0 : Y.has(Q)) || this._prefixes && !this._prefixes.has(Q)) throw Error(`CodeGen: prefix "${Q}" is not allowed in this scope`);
      return this._names[Q] = { prefix: Q, index: 0 };
    }
  }
  BG.Scope = Y$;
  class $$ extends x0.Name {
    constructor(Q, X) {
      super(X);
      this.prefix = Q;
    }
    setValue(Q, { property: X, itemIndex: Y }) {
      this.value = Q, this.scopePath = x0._`.${new x0.Name(X)}[${Y}]`;
    }
  }
  BG.ValueScopeName = $$;
  var lN = x0._`\n`;
  class HG extends Y$ {
    constructor(Q) {
      super(Q);
      this._values = {}, this._scope = Q.scope, this.opts = { ...Q, _n: Q.lines ? lN : x0.nil };
    }
    get() {
      return this._scope;
    }
    name(Q) {
      return new $$(Q, this._newName(Q));
    }
    value(Q, X) {
      var Y;
      if (X.ref === void 0) throw Error("CodeGen: ref must be passed in value");
      let $ = this.toName(Q), { prefix: W } = $, J = (Y = X.key) !== null && Y !== void 0 ? Y : X.ref, G = this._values[W];
      if (G) {
        let z = G.get(J);
        if (z) return z;
      } else G = this._values[W] = /* @__PURE__ */ new Map();
      G.set(J, $);
      let H = this._scope[W] || (this._scope[W] = []), B = H.length;
      return H[B] = X.ref, $.setValue(X, { property: W, itemIndex: B }), $;
    }
    getValue(Q, X) {
      let Y = this._values[Q];
      if (!Y) return;
      return Y.get(X);
    }
    scopeRefs(Q, X = this._values) {
      return this._reduceValues(X, (Y) => {
        if (Y.scopePath === void 0) throw Error(`CodeGen: name "${Y}" has no value`);
        return x0._`${Q}${Y.scopePath}`;
      });
    }
    scopeCode(Q = this._values, X, Y) {
      return this._reduceValues(Q, ($) => {
        if ($.value === void 0) throw Error(`CodeGen: name "${$}" has no value`);
        return $.value.code;
      }, X, Y);
    }
    _reduceValues(Q, X, Y = {}, $) {
      let W = x0.nil;
      for (let J in Q) {
        let G = Q[J];
        if (!G) continue;
        let H = Y[J] = Y[J] || /* @__PURE__ */ new Map();
        G.forEach((B) => {
          if (H.has(B)) return;
          H.set(B, A8.Started);
          let z = X(B);
          if (z) {
            let K = this.opts.es5 ? BG.varKinds.var : BG.varKinds.const;
            W = x0._`${W}${K} ${B} = ${z};${this.opts._n}`;
          } else if (z = $ === null || $ === void 0 ? void 0 : $(B)) W = x0._`${W}${z}${this.opts._n}`;
          else throw new GG(B);
          H.set(B, A8.Completed);
        });
      }
      return W;
    }
  }
  BG.ValueScope = HG;
});
var p = P((y0) => {
  Object.defineProperty(y0, "__esModule", { value: true });
  y0.or = y0.and = y0.not = y0.CodeGen = y0.operators = y0.varKinds = y0.ValueScopeName = y0.ValueScope = y0.Scope = y0.Name = y0.regexpCode = y0.stringify = y0.getProperty = y0.nil = y0.strConcat = y0.str = y0._ = void 0;
  var t = f4(), s0 = W$(), l1 = f4();
  Object.defineProperty(y0, "_", { enumerable: true, get: function() {
    return l1._;
  } });
  Object.defineProperty(y0, "str", { enumerable: true, get: function() {
    return l1.str;
  } });
  Object.defineProperty(y0, "strConcat", { enumerable: true, get: function() {
    return l1.strConcat;
  } });
  Object.defineProperty(y0, "nil", { enumerable: true, get: function() {
    return l1.nil;
  } });
  Object.defineProperty(y0, "getProperty", { enumerable: true, get: function() {
    return l1.getProperty;
  } });
  Object.defineProperty(y0, "stringify", { enumerable: true, get: function() {
    return l1.stringify;
  } });
  Object.defineProperty(y0, "regexpCode", { enumerable: true, get: function() {
    return l1.regexpCode;
  } });
  Object.defineProperty(y0, "Name", { enumerable: true, get: function() {
    return l1.Name;
  } });
  var P8 = W$();
  Object.defineProperty(y0, "Scope", { enumerable: true, get: function() {
    return P8.Scope;
  } });
  Object.defineProperty(y0, "ValueScope", { enumerable: true, get: function() {
    return P8.ValueScope;
  } });
  Object.defineProperty(y0, "ValueScopeName", { enumerable: true, get: function() {
    return P8.ValueScopeName;
  } });
  Object.defineProperty(y0, "varKinds", { enumerable: true, get: function() {
    return P8.varKinds;
  } });
  y0.operators = { GT: new t._Code(">"), GTE: new t._Code(">="), LT: new t._Code("<"), LTE: new t._Code("<="), EQ: new t._Code("==="), NEQ: new t._Code("!=="), NOT: new t._Code("!"), OR: new t._Code("||"), AND: new t._Code("&&"), ADD: new t._Code("+") };
  class m1 {
    optimizeNodes() {
      return this;
    }
    optimizeNames(Q, X) {
      return this;
    }
  }
  class KG extends m1 {
    constructor(Q, X, Y) {
      super();
      this.varKind = Q, this.name = X, this.rhs = Y;
    }
    render({ es5: Q, _n: X }) {
      let Y = Q ? s0.varKinds.var : this.varKind, $ = this.rhs === void 0 ? "" : ` = ${this.rhs}`;
      return `${Y} ${this.name}${$};` + X;
    }
    optimizeNames(Q, X) {
      if (!Q[this.name.str]) return;
      if (this.rhs) this.rhs = l6(this.rhs, Q, X);
      return this;
    }
    get names() {
      return this.rhs instanceof t._CodeOrName ? this.rhs.names : {};
    }
  }
  class H$ extends m1 {
    constructor(Q, X, Y) {
      super();
      this.lhs = Q, this.rhs = X, this.sideEffects = Y;
    }
    render({ _n: Q }) {
      return `${this.lhs} = ${this.rhs};` + Q;
    }
    optimizeNames(Q, X) {
      if (this.lhs instanceof t.Name && !Q[this.lhs.str] && !this.sideEffects) return;
      return this.rhs = l6(this.rhs, Q, X), this;
    }
    get names() {
      let Q = this.lhs instanceof t.Name ? {} : { ...this.lhs.names };
      return b8(Q, this.rhs);
    }
  }
  class VG extends H$ {
    constructor(Q, X, Y, $) {
      super(Q, Y, $);
      this.op = X;
    }
    render({ _n: Q }) {
      return `${this.lhs} ${this.op}= ${this.rhs};` + Q;
    }
  }
  class qG extends m1 {
    constructor(Q) {
      super();
      this.label = Q, this.names = {};
    }
    render({ _n: Q }) {
      return `${this.label}:` + Q;
    }
  }
  class UG extends m1 {
    constructor(Q) {
      super();
      this.label = Q, this.names = {};
    }
    render({ _n: Q }) {
      return `break${this.label ? ` ${this.label}` : ""};` + Q;
    }
  }
  class LG extends m1 {
    constructor(Q) {
      super();
      this.error = Q;
    }
    render({ _n: Q }) {
      return `throw ${this.error};` + Q;
    }
    get names() {
      return this.error.names;
    }
  }
  class FG extends m1 {
    constructor(Q) {
      super();
      this.code = Q;
    }
    render({ _n: Q }) {
      return `${this.code};` + Q;
    }
    optimizeNodes() {
      return `${this.code}` ? this : void 0;
    }
    optimizeNames(Q, X) {
      return this.code = l6(this.code, Q, X), this;
    }
    get names() {
      return this.code instanceof t._CodeOrName ? this.code.names : {};
    }
  }
  class S8 extends m1 {
    constructor(Q = []) {
      super();
      this.nodes = Q;
    }
    render(Q) {
      return this.nodes.reduce((X, Y) => X + Y.render(Q), "");
    }
    optimizeNodes() {
      let { nodes: Q } = this, X = Q.length;
      while (X--) {
        let Y = Q[X].optimizeNodes();
        if (Array.isArray(Y)) Q.splice(X, 1, ...Y);
        else if (Y) Q[X] = Y;
        else Q.splice(X, 1);
      }
      return Q.length > 0 ? this : void 0;
    }
    optimizeNames(Q, X) {
      let { nodes: Y } = this, $ = Y.length;
      while ($--) {
        let W = Y[$];
        if (W.optimizeNames(Q, X)) continue;
        dN(Q, W.names), Y.splice($, 1);
      }
      return Y.length > 0 ? this : void 0;
    }
    get names() {
      return this.nodes.reduce((Q, X) => H6(Q, X.names), {});
    }
  }
  class c1 extends S8 {
    render(Q) {
      return "{" + Q._n + super.render(Q) + "}" + Q._n;
    }
  }
  class NG extends S8 {
  }
  class u4 extends c1 {
  }
  u4.kind = "else";
  class j1 extends c1 {
    constructor(Q, X) {
      super(X);
      this.condition = Q;
    }
    render(Q) {
      let X = `if(${this.condition})` + super.render(Q);
      if (this.else) X += "else " + this.else.render(Q);
      return X;
    }
    optimizeNodes() {
      super.optimizeNodes();
      let Q = this.condition;
      if (Q === true) return this.nodes;
      let X = this.else;
      if (X) {
        let Y = X.optimizeNodes();
        X = this.else = Array.isArray(Y) ? new u4(Y) : Y;
      }
      if (X) {
        if (Q === false) return X instanceof j1 ? X : X.nodes;
        if (this.nodes.length) return this;
        return new j1(AG(Q), X instanceof j1 ? [X] : X.nodes);
      }
      if (Q === false || !this.nodes.length) return;
      return this;
    }
    optimizeNames(Q, X) {
      var Y;
      if (this.else = (Y = this.else) === null || Y === void 0 ? void 0 : Y.optimizeNames(Q, X), !(super.optimizeNames(Q, X) || this.else)) return;
      return this.condition = l6(this.condition, Q, X), this;
    }
    get names() {
      let Q = super.names;
      if (b8(Q, this.condition), this.else) H6(Q, this.else.names);
      return Q;
    }
  }
  j1.kind = "if";
  class u6 extends c1 {
  }
  u6.kind = "for";
  class DG extends u6 {
    constructor(Q) {
      super();
      this.iteration = Q;
    }
    render(Q) {
      return `for(${this.iteration})` + super.render(Q);
    }
    optimizeNames(Q, X) {
      if (!super.optimizeNames(Q, X)) return;
      return this.iteration = l6(this.iteration, Q, X), this;
    }
    get names() {
      return H6(super.names, this.iteration.names);
    }
  }
  class OG extends u6 {
    constructor(Q, X, Y, $) {
      super();
      this.varKind = Q, this.name = X, this.from = Y, this.to = $;
    }
    render(Q) {
      let X = Q.es5 ? s0.varKinds.var : this.varKind, { name: Y, from: $, to: W } = this;
      return `for(${X} ${Y}=${$}; ${Y}<${W}; ${Y}++)` + super.render(Q);
    }
    get names() {
      let Q = b8(super.names, this.from);
      return b8(Q, this.to);
    }
  }
  class J$ extends u6 {
    constructor(Q, X, Y, $) {
      super();
      this.loop = Q, this.varKind = X, this.name = Y, this.iterable = $;
    }
    render(Q) {
      return `for(${this.varKind} ${this.name} ${this.loop} ${this.iterable})` + super.render(Q);
    }
    optimizeNames(Q, X) {
      if (!super.optimizeNames(Q, X)) return;
      return this.iterable = l6(this.iterable, Q, X), this;
    }
    get names() {
      return H6(super.names, this.iterable.names);
    }
  }
  class j8 extends c1 {
    constructor(Q, X, Y) {
      super();
      this.name = Q, this.args = X, this.async = Y;
    }
    render(Q) {
      return `${this.async ? "async " : ""}function ${this.name}(${this.args})` + super.render(Q);
    }
  }
  j8.kind = "func";
  class R8 extends S8 {
    render(Q) {
      return "return " + super.render(Q);
    }
  }
  R8.kind = "return";
  class wG extends c1 {
    render(Q) {
      let X = "try" + super.render(Q);
      if (this.catch) X += this.catch.render(Q);
      if (this.finally) X += this.finally.render(Q);
      return X;
    }
    optimizeNodes() {
      var Q, X;
      return super.optimizeNodes(), (Q = this.catch) === null || Q === void 0 || Q.optimizeNodes(), (X = this.finally) === null || X === void 0 || X.optimizeNodes(), this;
    }
    optimizeNames(Q, X) {
      var Y, $;
      return super.optimizeNames(Q, X), (Y = this.catch) === null || Y === void 0 || Y.optimizeNames(Q, X), ($ = this.finally) === null || $ === void 0 || $.optimizeNames(Q, X), this;
    }
    get names() {
      let Q = super.names;
      if (this.catch) H6(Q, this.catch.names);
      if (this.finally) H6(Q, this.finally.names);
      return Q;
    }
  }
  class I8 extends c1 {
    constructor(Q) {
      super();
      this.error = Q;
    }
    render(Q) {
      return `catch(${this.error})` + super.render(Q);
    }
  }
  I8.kind = "catch";
  class E8 extends c1 {
    render(Q) {
      return "finally" + super.render(Q);
    }
  }
  E8.kind = "finally";
  class MG {
    constructor(Q, X = {}) {
      this._values = {}, this._blockStarts = [], this._constants = {}, this.opts = { ...X, _n: X.lines ? `
` : "" }, this._extScope = Q, this._scope = new s0.Scope({ parent: Q }), this._nodes = [new NG()];
    }
    toString() {
      return this._root.render(this.opts);
    }
    name(Q) {
      return this._scope.name(Q);
    }
    scopeName(Q) {
      return this._extScope.name(Q);
    }
    scopeValue(Q, X) {
      let Y = this._extScope.value(Q, X);
      return (this._values[Y.prefix] || (this._values[Y.prefix] = /* @__PURE__ */ new Set())).add(Y), Y;
    }
    getScopeValue(Q, X) {
      return this._extScope.getValue(Q, X);
    }
    scopeRefs(Q) {
      return this._extScope.scopeRefs(Q, this._values);
    }
    scopeCode() {
      return this._extScope.scopeCode(this._values);
    }
    _def(Q, X, Y, $) {
      let W = this._scope.toName(X);
      if (Y !== void 0 && $) this._constants[W.str] = Y;
      return this._leafNode(new KG(Q, W, Y)), W;
    }
    const(Q, X, Y) {
      return this._def(s0.varKinds.const, Q, X, Y);
    }
    let(Q, X, Y) {
      return this._def(s0.varKinds.let, Q, X, Y);
    }
    var(Q, X, Y) {
      return this._def(s0.varKinds.var, Q, X, Y);
    }
    assign(Q, X, Y) {
      return this._leafNode(new H$(Q, X, Y));
    }
    add(Q, X) {
      return this._leafNode(new VG(Q, y0.operators.ADD, X));
    }
    code(Q) {
      if (typeof Q == "function") Q();
      else if (Q !== t.nil) this._leafNode(new FG(Q));
      return this;
    }
    object(...Q) {
      let X = ["{"];
      for (let [Y, $] of Q) {
        if (X.length > 1) X.push(",");
        if (X.push(Y), Y !== $ || this.opts.es5) X.push(":"), (0, t.addCodeArg)(X, $);
      }
      return X.push("}"), new t._Code(X);
    }
    if(Q, X, Y) {
      if (this._blockNode(new j1(Q)), X && Y) this.code(X).else().code(Y).endIf();
      else if (X) this.code(X).endIf();
      else if (Y) throw Error('CodeGen: "else" body without "then" body');
      return this;
    }
    elseIf(Q) {
      return this._elseNode(new j1(Q));
    }
    else() {
      return this._elseNode(new u4());
    }
    endIf() {
      return this._endBlockNode(j1, u4);
    }
    _for(Q, X) {
      if (this._blockNode(Q), X) this.code(X).endFor();
      return this;
    }
    for(Q, X) {
      return this._for(new DG(Q), X);
    }
    forRange(Q, X, Y, $, W = this.opts.es5 ? s0.varKinds.var : s0.varKinds.let) {
      let J = this._scope.toName(Q);
      return this._for(new OG(W, J, X, Y), () => $(J));
    }
    forOf(Q, X, Y, $ = s0.varKinds.const) {
      let W = this._scope.toName(Q);
      if (this.opts.es5) {
        let J = X instanceof t.Name ? X : this.var("_arr", X);
        return this.forRange("_i", 0, t._`${J}.length`, (G) => {
          this.var(W, t._`${J}[${G}]`), Y(W);
        });
      }
      return this._for(new J$("of", $, W, X), () => Y(W));
    }
    forIn(Q, X, Y, $ = this.opts.es5 ? s0.varKinds.var : s0.varKinds.const) {
      if (this.opts.ownProperties) return this.forOf(Q, t._`Object.keys(${X})`, Y);
      let W = this._scope.toName(Q);
      return this._for(new J$("in", $, W, X), () => Y(W));
    }
    endFor() {
      return this._endBlockNode(u6);
    }
    label(Q) {
      return this._leafNode(new qG(Q));
    }
    break(Q) {
      return this._leafNode(new UG(Q));
    }
    return(Q) {
      let X = new R8();
      if (this._blockNode(X), this.code(Q), X.nodes.length !== 1) throw Error('CodeGen: "return" should have one node');
      return this._endBlockNode(R8);
    }
    try(Q, X, Y) {
      if (!X && !Y) throw Error('CodeGen: "try" without "catch" and "finally"');
      let $ = new wG();
      if (this._blockNode($), this.code(Q), X) {
        let W = this.name("e");
        this._currNode = $.catch = new I8(W), X(W);
      }
      if (Y) this._currNode = $.finally = new E8(), this.code(Y);
      return this._endBlockNode(I8, E8);
    }
    throw(Q) {
      return this._leafNode(new LG(Q));
    }
    block(Q, X) {
      if (this._blockStarts.push(this._nodes.length), Q) this.code(Q).endBlock(X);
      return this;
    }
    endBlock(Q) {
      let X = this._blockStarts.pop();
      if (X === void 0) throw Error("CodeGen: not in self-balancing block");
      let Y = this._nodes.length - X;
      if (Y < 0 || Q !== void 0 && Y !== Q) throw Error(`CodeGen: wrong number of nodes: ${Y} vs ${Q} expected`);
      return this._nodes.length = X, this;
    }
    func(Q, X = t.nil, Y, $) {
      if (this._blockNode(new j8(Q, X, Y)), $) this.code($).endFunc();
      return this;
    }
    endFunc() {
      return this._endBlockNode(j8);
    }
    optimize(Q = 1) {
      while (Q-- > 0) this._root.optimizeNodes(), this._root.optimizeNames(this._root.names, this._constants);
    }
    _leafNode(Q) {
      return this._currNode.nodes.push(Q), this;
    }
    _blockNode(Q) {
      this._currNode.nodes.push(Q), this._nodes.push(Q);
    }
    _endBlockNode(Q, X) {
      let Y = this._currNode;
      if (Y instanceof Q || X && Y instanceof X) return this._nodes.pop(), this;
      throw Error(`CodeGen: not in block "${X ? `${Q.kind}/${X.kind}` : Q.kind}"`);
    }
    _elseNode(Q) {
      let X = this._currNode;
      if (!(X instanceof j1)) throw Error('CodeGen: "else" without "if"');
      return this._currNode = X.else = Q, this;
    }
    get _root() {
      return this._nodes[0];
    }
    get _currNode() {
      let Q = this._nodes;
      return Q[Q.length - 1];
    }
    set _currNode(Q) {
      let X = this._nodes;
      X[X.length - 1] = Q;
    }
  }
  y0.CodeGen = MG;
  function H6(Q, X) {
    for (let Y in X) Q[Y] = (Q[Y] || 0) + (X[Y] || 0);
    return Q;
  }
  function b8(Q, X) {
    return X instanceof t._CodeOrName ? H6(Q, X.names) : Q;
  }
  function l6(Q, X, Y) {
    if (Q instanceof t.Name) return $(Q);
    if (!W(Q)) return Q;
    return new t._Code(Q._items.reduce((J, G) => {
      if (G instanceof t.Name) G = $(G);
      if (G instanceof t._Code) J.push(...G._items);
      else J.push(G);
      return J;
    }, []));
    function $(J) {
      let G = Y[J.str];
      if (G === void 0 || X[J.str] !== 1) return J;
      return delete X[J.str], G;
    }
    function W(J) {
      return J instanceof t._Code && J._items.some((G) => G instanceof t.Name && X[G.str] === 1 && Y[G.str] !== void 0);
    }
  }
  function dN(Q, X) {
    for (let Y in X) Q[Y] = (Q[Y] || 0) - (X[Y] || 0);
  }
  function AG(Q) {
    return typeof Q == "boolean" || typeof Q == "number" || Q === null ? !Q : t._`!${G$(Q)}`;
  }
  y0.not = AG;
  var iN = jG(y0.operators.AND);
  function nN(...Q) {
    return Q.reduce(iN);
  }
  y0.and = nN;
  var rN = jG(y0.operators.OR);
  function oN(...Q) {
    return Q.reduce(rN);
  }
  y0.or = oN;
  function jG(Q) {
    return (X, Y) => X === t.nil ? Y : Y === t.nil ? X : t._`${G$(X)} ${Q} ${G$(Y)}`;
  }
  function G$(Q) {
    return Q instanceof t.Name ? Q : t._`(${Q})`;
  }
});
var e = P((kG) => {
  Object.defineProperty(kG, "__esModule", { value: true });
  kG.checkStrictMode = kG.getErrorPath = kG.Type = kG.useFunc = kG.setEvaluated = kG.evaluatedPropsToName = kG.mergeEvaluated = kG.eachItem = kG.unescapeJsonPointer = kG.escapeJsonPointer = kG.escapeFragment = kG.unescapeFragment = kG.schemaRefOrVal = kG.schemaHasRulesButRef = kG.schemaHasRules = kG.checkUnknownRules = kG.alwaysValidSchema = kG.toHash = void 0;
  var Y0 = p(), eN = f4();
  function QD(Q) {
    let X = {};
    for (let Y of Q) X[Y] = true;
    return X;
  }
  kG.toHash = QD;
  function XD(Q, X) {
    if (typeof X == "boolean") return X;
    if (Object.keys(X).length === 0) return true;
    return bG(Q, X), !PG(X, Q.self.RULES.all);
  }
  kG.alwaysValidSchema = XD;
  function bG(Q, X = Q.schema) {
    let { opts: Y, self: $ } = Q;
    if (!Y.strictSchema) return;
    if (typeof X === "boolean") return;
    let W = $.RULES.keywords;
    for (let J in X) if (!W[J]) CG(Q, `unknown keyword: "${J}"`);
  }
  kG.checkUnknownRules = bG;
  function PG(Q, X) {
    if (typeof Q == "boolean") return !Q;
    for (let Y in Q) if (X[Y]) return true;
    return false;
  }
  kG.schemaHasRules = PG;
  function YD(Q, X) {
    if (typeof Q == "boolean") return !Q;
    for (let Y in Q) if (Y !== "$ref" && X.all[Y]) return true;
    return false;
  }
  kG.schemaHasRulesButRef = YD;
  function $D({ topSchemaRef: Q, schemaPath: X }, Y, $, W) {
    if (!W) {
      if (typeof Y == "number" || typeof Y == "boolean") return Y;
      if (typeof Y == "string") return Y0._`${Y}`;
    }
    return Y0._`${Q}${X}${(0, Y0.getProperty)($)}`;
  }
  kG.schemaRefOrVal = $D;
  function WD(Q) {
    return SG(decodeURIComponent(Q));
  }
  kG.unescapeFragment = WD;
  function JD(Q) {
    return encodeURIComponent(z$(Q));
  }
  kG.escapeFragment = JD;
  function z$(Q) {
    if (typeof Q == "number") return `${Q}`;
    return Q.replace(/~/g, "~0").replace(/\//g, "~1");
  }
  kG.escapeJsonPointer = z$;
  function SG(Q) {
    return Q.replace(/~1/g, "/").replace(/~0/g, "~");
  }
  kG.unescapeJsonPointer = SG;
  function GD(Q, X) {
    if (Array.isArray(Q)) for (let Y of Q) X(Y);
    else X(Q);
  }
  kG.eachItem = GD;
  function IG({ mergeNames: Q, mergeToName: X, mergeValues: Y, resultToName: $ }) {
    return (W, J, G, H) => {
      let B = G === void 0 ? J : G instanceof Y0.Name ? (J instanceof Y0.Name ? Q(W, J, G) : X(W, J, G), G) : J instanceof Y0.Name ? (X(W, G, J), J) : Y(J, G);
      return H === Y0.Name && !(B instanceof Y0.Name) ? $(W, B) : B;
    };
  }
  kG.mergeEvaluated = { props: IG({ mergeNames: (Q, X, Y) => Q.if(Y0._`${Y} !== true && ${X} !== undefined`, () => {
    Q.if(Y0._`${X} === true`, () => Q.assign(Y, true), () => Q.assign(Y, Y0._`${Y} || {}`).code(Y0._`Object.assign(${Y}, ${X})`));
  }), mergeToName: (Q, X, Y) => Q.if(Y0._`${Y} !== true`, () => {
    if (X === true) Q.assign(Y, true);
    else Q.assign(Y, Y0._`${Y} || {}`), K$(Q, Y, X);
  }), mergeValues: (Q, X) => Q === true ? true : { ...Q, ...X }, resultToName: ZG }), items: IG({ mergeNames: (Q, X, Y) => Q.if(Y0._`${Y} !== true && ${X} !== undefined`, () => Q.assign(Y, Y0._`${X} === true ? true : ${Y} > ${X} ? ${Y} : ${X}`)), mergeToName: (Q, X, Y) => Q.if(Y0._`${Y} !== true`, () => Q.assign(Y, X === true ? true : Y0._`${Y} > ${X} ? ${Y} : ${X}`)), mergeValues: (Q, X) => Q === true ? true : Math.max(Q, X), resultToName: (Q, X) => Q.var("items", X) }) };
  function ZG(Q, X) {
    if (X === true) return Q.var("props", true);
    let Y = Q.var("props", Y0._`{}`);
    if (X !== void 0) K$(Q, Y, X);
    return Y;
  }
  kG.evaluatedPropsToName = ZG;
  function K$(Q, X, Y) {
    Object.keys(Y).forEach(($) => Q.assign(Y0._`${X}${(0, Y0.getProperty)($)}`, true));
  }
  kG.setEvaluated = K$;
  var EG = {};
  function HD(Q, X) {
    return Q.scopeValue("func", { ref: X, code: EG[X.code] || (EG[X.code] = new eN._Code(X.code)) });
  }
  kG.useFunc = HD;
  var B$;
  (function(Q) {
    Q[Q.Num = 0] = "Num", Q[Q.Str = 1] = "Str";
  })(B$ || (kG.Type = B$ = {}));
  function BD(Q, X, Y) {
    if (Q instanceof Y0.Name) {
      let $ = X === B$.Num;
      return Y ? $ ? Y0._`"[" + ${Q} + "]"` : Y0._`"['" + ${Q} + "']"` : $ ? Y0._`"/" + ${Q}` : Y0._`"/" + ${Q}.replace(/~/g, "~0").replace(/\\//g, "~1")`;
    }
    return Y ? (0, Y0.getProperty)(Q).toString() : "/" + z$(Q);
  }
  kG.getErrorPath = BD;
  function CG(Q, X, Y = Q.opts.strictSchema) {
    if (!Y) return;
    if (X = `strict mode: ${X}`, Y === true) throw Error(X);
    Q.self.logger.warn(X);
  }
  kG.checkStrictMode = CG;
});
var R1 = P((_G) => {
  Object.defineProperty(_G, "__esModule", { value: true });
  var S0 = p(), bD = { data: new S0.Name("data"), valCxt: new S0.Name("valCxt"), instancePath: new S0.Name("instancePath"), parentData: new S0.Name("parentData"), parentDataProperty: new S0.Name("parentDataProperty"), rootData: new S0.Name("rootData"), dynamicAnchors: new S0.Name("dynamicAnchors"), vErrors: new S0.Name("vErrors"), errors: new S0.Name("errors"), this: new S0.Name("this"), self: new S0.Name("self"), scope: new S0.Name("scope"), json: new S0.Name("json"), jsonPos: new S0.Name("jsonPos"), jsonLen: new S0.Name("jsonLen"), jsonPart: new S0.Name("jsonPart") };
  _G.default = bD;
});
var l4 = P((gG) => {
  Object.defineProperty(gG, "__esModule", { value: true });
  gG.extendErrors = gG.resetErrorsCount = gG.reportExtraError = gG.reportError = gG.keyword$DataError = gG.keywordError = void 0;
  var a = p(), C8 = e(), v0 = R1();
  gG.keywordError = { message: ({ keyword: Q }) => a.str`must pass "${Q}" keyword validation` };
  gG.keyword$DataError = { message: ({ keyword: Q, schemaType: X }) => X ? a.str`"${Q}" keyword must be ${X} ($data)` : a.str`"${Q}" keyword is invalid ($data)` };
  function SD(Q, X = gG.keywordError, Y, $) {
    let { it: W } = Q, { gen: J, compositeRule: G, allErrors: H } = W, B = yG(Q, X, Y);
    if ($ !== null && $ !== void 0 ? $ : G || H) TG(J, B);
    else xG(W, a._`[${B}]`);
  }
  gG.reportError = SD;
  function ZD(Q, X = gG.keywordError, Y) {
    let { it: $ } = Q, { gen: W, compositeRule: J, allErrors: G } = $, H = yG(Q, X, Y);
    if (TG(W, H), !(J || G)) xG($, v0.default.vErrors);
  }
  gG.reportExtraError = ZD;
  function CD(Q, X) {
    Q.assign(v0.default.errors, X), Q.if(a._`${v0.default.vErrors} !== null`, () => Q.if(X, () => Q.assign(a._`${v0.default.vErrors}.length`, X), () => Q.assign(v0.default.vErrors, null)));
  }
  gG.resetErrorsCount = CD;
  function kD({ gen: Q, keyword: X, schemaValue: Y, data: $, errsCount: W, it: J }) {
    if (W === void 0) throw Error("ajv implementation error");
    let G = Q.name("err");
    Q.forRange("i", W, v0.default.errors, (H) => {
      if (Q.const(G, a._`${v0.default.vErrors}[${H}]`), Q.if(a._`${G}.instancePath === undefined`, () => Q.assign(a._`${G}.instancePath`, (0, a.strConcat)(v0.default.instancePath, J.errorPath))), Q.assign(a._`${G}.schemaPath`, a.str`${J.errSchemaPath}/${X}`), J.opts.verbose) Q.assign(a._`${G}.schema`, Y), Q.assign(a._`${G}.data`, $);
    });
  }
  gG.extendErrors = kD;
  function TG(Q, X) {
    let Y = Q.const("err", X);
    Q.if(a._`${v0.default.vErrors} === null`, () => Q.assign(v0.default.vErrors, a._`[${Y}]`), a._`${v0.default.vErrors}.push(${Y})`), Q.code(a._`${v0.default.errors}++`);
  }
  function xG(Q, X) {
    let { gen: Y, validateName: $, schemaEnv: W } = Q;
    if (W.$async) Y.throw(a._`new ${Q.ValidationError}(${X})`);
    else Y.assign(a._`${$}.errors`, X), Y.return(false);
  }
  var B6 = { keyword: new a.Name("keyword"), schemaPath: new a.Name("schemaPath"), params: new a.Name("params"), propertyName: new a.Name("propertyName"), message: new a.Name("message"), schema: new a.Name("schema"), parentSchema: new a.Name("parentSchema") };
  function yG(Q, X, Y) {
    let { createErrors: $ } = Q.it;
    if ($ === false) return a._`{}`;
    return vD(Q, X, Y);
  }
  function vD(Q, X, Y = {}) {
    let { gen: $, it: W } = Q, J = [_D(W, Y), TD(Q, Y)];
    return xD(Q, X, J), $.object(...J);
  }
  function _D({ errorPath: Q }, { instancePath: X }) {
    let Y = X ? a.str`${Q}${(0, C8.getErrorPath)(X, C8.Type.Str)}` : Q;
    return [v0.default.instancePath, (0, a.strConcat)(v0.default.instancePath, Y)];
  }
  function TD({ keyword: Q, it: { errSchemaPath: X } }, { schemaPath: Y, parentSchema: $ }) {
    let W = $ ? X : a.str`${X}/${Q}`;
    if (Y) W = a.str`${W}${(0, C8.getErrorPath)(Y, C8.Type.Str)}`;
    return [B6.schemaPath, W];
  }
  function xD(Q, { params: X, message: Y }, $) {
    let { keyword: W, data: J, schemaValue: G, it: H } = Q, { opts: B, propertyName: z, topSchemaRef: K, schemaPath: q } = H;
    if ($.push([B6.keyword, W], [B6.params, typeof X == "function" ? X(Q) : X || a._`{}`]), B.messages) $.push([B6.message, typeof Y == "function" ? Y(Q) : Y]);
    if (B.verbose) $.push([B6.schema, G], [B6.parentSchema, a._`${K}${q}`], [v0.default.data, J]);
    if (z) $.push([B6.propertyName, z]);
  }
});
var mG = P((uG) => {
  Object.defineProperty(uG, "__esModule", { value: true });
  uG.boolOrEmptySchema = uG.topBoolOrEmptySchema = void 0;
  var uD = l4(), lD = p(), mD = R1(), cD = { message: "boolean schema is false" };
  function pD(Q) {
    let { gen: X, schema: Y, validateName: $ } = Q;
    if (Y === false) fG(Q, false);
    else if (typeof Y == "object" && Y.$async === true) X.return(mD.default.data);
    else X.assign(lD._`${$}.errors`, null), X.return(true);
  }
  uG.topBoolOrEmptySchema = pD;
  function dD(Q, X) {
    let { gen: Y, schema: $ } = Q;
    if ($ === false) Y.var(X, false), fG(Q);
    else Y.var(X, true);
  }
  uG.boolOrEmptySchema = dD;
  function fG(Q, X) {
    let { gen: Y, data: $ } = Q, W = { gen: Y, keyword: "false schema", data: $, schema: false, schemaCode: false, schemaValue: false, params: {}, it: Q };
    (0, uD.reportError)(W, cD, void 0, X);
  }
});
var q$ = P((cG) => {
  Object.defineProperty(cG, "__esModule", { value: true });
  cG.getRules = cG.isJSONType = void 0;
  var nD = ["string", "number", "integer", "boolean", "null", "object", "array"], rD = new Set(nD);
  function oD(Q) {
    return typeof Q == "string" && rD.has(Q);
  }
  cG.isJSONType = oD;
  function tD() {
    let Q = { number: { type: "number", rules: [] }, string: { type: "string", rules: [] }, array: { type: "array", rules: [] }, object: { type: "object", rules: [] } };
    return { types: { ...Q, integer: true, boolean: true, null: true }, rules: [{ rules: [] }, Q.number, Q.string, Q.array, Q.object], post: { rules: [] }, all: {}, keywords: {} };
  }
  cG.getRules = tD;
});
var U$ = P((nG) => {
  Object.defineProperty(nG, "__esModule", { value: true });
  nG.shouldUseRule = nG.shouldUseGroup = nG.schemaHasRulesForType = void 0;
  function sD({ schema: Q, self: X }, Y) {
    let $ = X.RULES.types[Y];
    return $ && $ !== true && dG(Q, $);
  }
  nG.schemaHasRulesForType = sD;
  function dG(Q, X) {
    return X.rules.some((Y) => iG(Q, Y));
  }
  nG.shouldUseGroup = dG;
  function iG(Q, X) {
    var Y;
    return Q[X.keyword] !== void 0 || ((Y = X.definition.implements) === null || Y === void 0 ? void 0 : Y.some(($) => Q[$] !== void 0));
  }
  nG.shouldUseRule = iG;
});
var m4 = P((sG) => {
  Object.defineProperty(sG, "__esModule", { value: true });
  sG.reportTypeError = sG.checkDataTypes = sG.checkDataType = sG.coerceAndCheckDataType = sG.getJSONTypes = sG.getSchemaTypes = sG.DataType = void 0;
  var XO = q$(), YO = U$(), $O = l4(), c = p(), oG = e(), m6;
  (function(Q) {
    Q[Q.Correct = 0] = "Correct", Q[Q.Wrong = 1] = "Wrong";
  })(m6 || (sG.DataType = m6 = {}));
  function WO(Q) {
    let X = tG(Q.type);
    if (X.includes("null")) {
      if (Q.nullable === false) throw Error("type: null contradicts nullable: false");
    } else {
      if (!X.length && Q.nullable !== void 0) throw Error('"nullable" cannot be used without "type"');
      if (Q.nullable === true) X.push("null");
    }
    return X;
  }
  sG.getSchemaTypes = WO;
  function tG(Q) {
    let X = Array.isArray(Q) ? Q : Q ? [Q] : [];
    if (X.every(XO.isJSONType)) return X;
    throw Error("type must be JSONType or JSONType[]: " + X.join(","));
  }
  sG.getJSONTypes = tG;
  function JO(Q, X) {
    let { gen: Y, data: $, opts: W } = Q, J = GO(X, W.coerceTypes), G = X.length > 0 && !(J.length === 0 && X.length === 1 && (0, YO.schemaHasRulesForType)(Q, X[0]));
    if (G) {
      let H = F$(X, $, W.strictNumbers, m6.Wrong);
      Y.if(H, () => {
        if (J.length) HO(Q, X, J);
        else N$(Q);
      });
    }
    return G;
  }
  sG.coerceAndCheckDataType = JO;
  var aG = /* @__PURE__ */ new Set(["string", "number", "integer", "boolean", "null"]);
  function GO(Q, X) {
    return X ? Q.filter((Y) => aG.has(Y) || X === "array" && Y === "array") : [];
  }
  function HO(Q, X, Y) {
    let { gen: $, data: W, opts: J } = Q, G = $.let("dataType", c._`typeof ${W}`), H = $.let("coerced", c._`undefined`);
    if (J.coerceTypes === "array") $.if(c._`${G} == 'object' && Array.isArray(${W}) && ${W}.length == 1`, () => $.assign(W, c._`${W}[0]`).assign(G, c._`typeof ${W}`).if(F$(X, W, J.strictNumbers), () => $.assign(H, W)));
    $.if(c._`${H} !== undefined`);
    for (let z of Y) if (aG.has(z) || z === "array" && J.coerceTypes === "array") B(z);
    $.else(), N$(Q), $.endIf(), $.if(c._`${H} !== undefined`, () => {
      $.assign(W, H), BO(Q, H);
    });
    function B(z) {
      switch (z) {
        case "string":
          $.elseIf(c._`${G} == "number" || ${G} == "boolean"`).assign(H, c._`"" + ${W}`).elseIf(c._`${W} === null`).assign(H, c._`""`);
          return;
        case "number":
          $.elseIf(c._`${G} == "boolean" || ${W} === null
              || (${G} == "string" && ${W} && ${W} == +${W})`).assign(H, c._`+${W}`);
          return;
        case "integer":
          $.elseIf(c._`${G} === "boolean" || ${W} === null
              || (${G} === "string" && ${W} && ${W} == +${W} && !(${W} % 1))`).assign(H, c._`+${W}`);
          return;
        case "boolean":
          $.elseIf(c._`${W} === "false" || ${W} === 0 || ${W} === null`).assign(H, false).elseIf(c._`${W} === "true" || ${W} === 1`).assign(H, true);
          return;
        case "null":
          $.elseIf(c._`${W} === "" || ${W} === 0 || ${W} === false`), $.assign(H, null);
          return;
        case "array":
          $.elseIf(c._`${G} === "string" || ${G} === "number"
              || ${G} === "boolean" || ${W} === null`).assign(H, c._`[${W}]`);
      }
    }
  }
  function BO({ gen: Q, parentData: X, parentDataProperty: Y }, $) {
    Q.if(c._`${X} !== undefined`, () => Q.assign(c._`${X}[${Y}]`, $));
  }
  function L$(Q, X, Y, $ = m6.Correct) {
    let W = $ === m6.Correct ? c.operators.EQ : c.operators.NEQ, J;
    switch (Q) {
      case "null":
        return c._`${X} ${W} null`;
      case "array":
        J = c._`Array.isArray(${X})`;
        break;
      case "object":
        J = c._`${X} && typeof ${X} == "object" && !Array.isArray(${X})`;
        break;
      case "integer":
        J = G(c._`!(${X} % 1) && !isNaN(${X})`);
        break;
      case "number":
        J = G();
        break;
      default:
        return c._`typeof ${X} ${W} ${Q}`;
    }
    return $ === m6.Correct ? J : (0, c.not)(J);
    function G(H = c.nil) {
      return (0, c.and)(c._`typeof ${X} == "number"`, H, Y ? c._`isFinite(${X})` : c.nil);
    }
  }
  sG.checkDataType = L$;
  function F$(Q, X, Y, $) {
    if (Q.length === 1) return L$(Q[0], X, Y, $);
    let W, J = (0, oG.toHash)(Q);
    if (J.array && J.object) {
      let G = c._`typeof ${X} != "object"`;
      W = J.null ? G : c._`!${X} || ${G}`, delete J.null, delete J.array, delete J.object;
    } else W = c.nil;
    if (J.number) delete J.integer;
    for (let G in J) W = (0, c.and)(W, L$(G, X, Y, $));
    return W;
  }
  sG.checkDataTypes = F$;
  var zO = { message: ({ schema: Q }) => `must be ${Q}`, params: ({ schema: Q, schemaValue: X }) => typeof Q == "string" ? c._`{type: ${Q}}` : c._`{type: ${X}}` };
  function N$(Q) {
    let X = KO(Q);
    (0, $O.reportError)(X, zO);
  }
  sG.reportTypeError = N$;
  function KO(Q) {
    let { gen: X, data: Y, schema: $ } = Q, W = (0, oG.schemaRefOrVal)(Q, $, "type");
    return { gen: X, keyword: "type", data: Y, schema: $.type, schemaCode: W, schemaValue: W, parentSchema: $, params: {}, it: Q };
  }
});
var $3 = P((X3) => {
  Object.defineProperty(X3, "__esModule", { value: true });
  X3.assignDefaults = void 0;
  var c6 = p(), DO = e();
  function OO(Q, X) {
    let { properties: Y, items: $ } = Q.schema;
    if (X === "object" && Y) for (let W in Y) Q3(Q, W, Y[W].default);
    else if (X === "array" && Array.isArray($)) $.forEach((W, J) => Q3(Q, J, W.default));
  }
  X3.assignDefaults = OO;
  function Q3(Q, X, Y) {
    let { gen: $, compositeRule: W, data: J, opts: G } = Q;
    if (Y === void 0) return;
    let H = c6._`${J}${(0, c6.getProperty)(X)}`;
    if (W) {
      (0, DO.checkStrictMode)(Q, `default is ignored for: ${H}`);
      return;
    }
    let B = c6._`${H} === undefined`;
    if (G.useDefaults === "empty") B = c6._`${B} || ${H} === null || ${H} === ""`;
    $.if(B, c6._`${H} = ${(0, c6.stringify)(Y)}`);
  }
});
var d0 = P((G3) => {
  Object.defineProperty(G3, "__esModule", { value: true });
  G3.validateUnion = G3.validateArray = G3.usePattern = G3.callValidateCode = G3.schemaProperties = G3.allSchemaProperties = G3.noPropertyInData = G3.propertyInData = G3.isOwnProperty = G3.hasPropFunc = G3.reportMissingProp = G3.checkMissingProp = G3.checkReportMissingProp = void 0;
  var G0 = p(), D$ = e(), p1 = R1(), wO = e();
  function MO(Q, X) {
    let { gen: Y, data: $, it: W } = Q;
    Y.if(w$(Y, $, X, W.opts.ownProperties), () => {
      Q.setParams({ missingProperty: G0._`${X}` }, true), Q.error();
    });
  }
  G3.checkReportMissingProp = MO;
  function AO({ gen: Q, data: X, it: { opts: Y } }, $, W) {
    return (0, G0.or)(...$.map((J) => (0, G0.and)(w$(Q, X, J, Y.ownProperties), G0._`${W} = ${J}`)));
  }
  G3.checkMissingProp = AO;
  function jO(Q, X) {
    Q.setParams({ missingProperty: X }, true), Q.error();
  }
  G3.reportMissingProp = jO;
  function W3(Q) {
    return Q.scopeValue("func", { ref: Object.prototype.hasOwnProperty, code: G0._`Object.prototype.hasOwnProperty` });
  }
  G3.hasPropFunc = W3;
  function O$(Q, X, Y) {
    return G0._`${W3(Q)}.call(${X}, ${Y})`;
  }
  G3.isOwnProperty = O$;
  function RO(Q, X, Y, $) {
    let W = G0._`${X}${(0, G0.getProperty)(Y)} !== undefined`;
    return $ ? G0._`${W} && ${O$(Q, X, Y)}` : W;
  }
  G3.propertyInData = RO;
  function w$(Q, X, Y, $) {
    let W = G0._`${X}${(0, G0.getProperty)(Y)} === undefined`;
    return $ ? (0, G0.or)(W, (0, G0.not)(O$(Q, X, Y))) : W;
  }
  G3.noPropertyInData = w$;
  function J3(Q) {
    return Q ? Object.keys(Q).filter((X) => X !== "__proto__") : [];
  }
  G3.allSchemaProperties = J3;
  function IO(Q, X) {
    return J3(X).filter((Y) => !(0, D$.alwaysValidSchema)(Q, X[Y]));
  }
  G3.schemaProperties = IO;
  function EO({ schemaCode: Q, data: X, it: { gen: Y, topSchemaRef: $, schemaPath: W, errorPath: J }, it: G }, H, B, z) {
    let K = z ? G0._`${Q}, ${X}, ${$}${W}` : X, q = [[p1.default.instancePath, (0, G0.strConcat)(p1.default.instancePath, J)], [p1.default.parentData, G.parentData], [p1.default.parentDataProperty, G.parentDataProperty], [p1.default.rootData, p1.default.rootData]];
    if (G.opts.dynamicRef) q.push([p1.default.dynamicAnchors, p1.default.dynamicAnchors]);
    let U = G0._`${K}, ${Y.object(...q)}`;
    return B !== G0.nil ? G0._`${H}.call(${B}, ${U})` : G0._`${H}(${U})`;
  }
  G3.callValidateCode = EO;
  var bO = G0._`new RegExp`;
  function PO({ gen: Q, it: { opts: X } }, Y) {
    let $ = X.unicodeRegExp ? "u" : "", { regExp: W } = X.code, J = W(Y, $);
    return Q.scopeValue("pattern", { key: J.toString(), ref: J, code: G0._`${W.code === "new RegExp" ? bO : (0, wO.useFunc)(Q, W)}(${Y}, ${$})` });
  }
  G3.usePattern = PO;
  function SO(Q) {
    let { gen: X, data: Y, keyword: $, it: W } = Q, J = X.name("valid");
    if (W.allErrors) {
      let H = X.let("valid", true);
      return G(() => X.assign(H, false)), H;
    }
    return X.var(J, true), G(() => X.break()), J;
    function G(H) {
      let B = X.const("len", G0._`${Y}.length`);
      X.forRange("i", 0, B, (z) => {
        Q.subschema({ keyword: $, dataProp: z, dataPropType: D$.Type.Num }, J), X.if((0, G0.not)(J), H);
      });
    }
  }
  G3.validateArray = SO;
  function ZO(Q) {
    let { gen: X, schema: Y, keyword: $, it: W } = Q;
    if (!Array.isArray(Y)) throw Error("ajv implementation error");
    if (Y.some((B) => (0, D$.alwaysValidSchema)(W, B)) && !W.opts.unevaluated) return;
    let G = X.let("valid", false), H = X.name("_valid");
    X.block(() => Y.forEach((B, z) => {
      let K = Q.subschema({ keyword: $, schemaProp: z, compositeRule: true }, H);
      if (X.assign(G, G0._`${G} || ${H}`), !Q.mergeValidEvaluated(K, H)) X.if((0, G0.not)(G));
    })), Q.result(G, () => Q.reset(), () => Q.error(true));
  }
  G3.validateUnion = ZO;
});
var q3 = P((K3) => {
  Object.defineProperty(K3, "__esModule", { value: true });
  K3.validateKeywordUsage = K3.validSchemaType = K3.funcKeywordCode = K3.macroKeywordCode = void 0;
  var _0 = p(), z6 = R1(), mO = d0(), cO = l4();
  function pO(Q, X) {
    let { gen: Y, keyword: $, schema: W, parentSchema: J, it: G } = Q, H = X.macro.call(G.self, W, J, G), B = z3(Y, $, H);
    if (G.opts.validateSchema !== false) G.self.validateSchema(H, true);
    let z = Y.name("valid");
    Q.subschema({ schema: H, schemaPath: _0.nil, errSchemaPath: `${G.errSchemaPath}/${$}`, topSchemaRef: B, compositeRule: true }, z), Q.pass(z, () => Q.error(true));
  }
  K3.macroKeywordCode = pO;
  function dO(Q, X) {
    var Y;
    let { gen: $, keyword: W, schema: J, parentSchema: G, $data: H, it: B } = Q;
    nO(B, X);
    let z = !H && X.compile ? X.compile.call(B.self, J, G, B) : X.validate, K = z3($, W, z), q = $.let("valid");
    Q.block$data(q, U), Q.ok((Y = X.valid) !== null && Y !== void 0 ? Y : q);
    function U() {
      if (X.errors === false) {
        if (L(), X.modifying) B3(Q);
        N(() => Q.error());
      } else {
        let w = X.async ? V() : F();
        if (X.modifying) B3(Q);
        N(() => iO(Q, w));
      }
    }
    function V() {
      let w = $.let("ruleErrs", null);
      return $.try(() => L(_0._`await `), (A) => $.assign(q, false).if(_0._`${A} instanceof ${B.ValidationError}`, () => $.assign(w, _0._`${A}.errors`), () => $.throw(A))), w;
    }
    function F() {
      let w = _0._`${K}.errors`;
      return $.assign(w, null), L(_0.nil), w;
    }
    function L(w = X.async ? _0._`await ` : _0.nil) {
      let A = B.opts.passContext ? z6.default.this : z6.default.self, R = !("compile" in X && !H || X.schema === false);
      $.assign(q, _0._`${w}${(0, mO.callValidateCode)(Q, K, A, R)}`, X.modifying);
    }
    function N(w) {
      var A;
      $.if((0, _0.not)((A = X.valid) !== null && A !== void 0 ? A : q), w);
    }
  }
  K3.funcKeywordCode = dO;
  function B3(Q) {
    let { gen: X, data: Y, it: $ } = Q;
    X.if($.parentData, () => X.assign(Y, _0._`${$.parentData}[${$.parentDataProperty}]`));
  }
  function iO(Q, X) {
    let { gen: Y } = Q;
    Y.if(_0._`Array.isArray(${X})`, () => {
      Y.assign(z6.default.vErrors, _0._`${z6.default.vErrors} === null ? ${X} : ${z6.default.vErrors}.concat(${X})`).assign(z6.default.errors, _0._`${z6.default.vErrors}.length`), (0, cO.extendErrors)(Q);
    }, () => Q.error());
  }
  function nO({ schemaEnv: Q }, X) {
    if (X.async && !Q.$async) throw Error("async keyword in sync schema");
  }
  function z3(Q, X, Y) {
    if (Y === void 0) throw Error(`keyword "${X}" failed to compile`);
    return Q.scopeValue("keyword", typeof Y == "function" ? { ref: Y } : { ref: Y, code: (0, _0.stringify)(Y) });
  }
  function rO(Q, X, Y = false) {
    return !X.length || X.some(($) => $ === "array" ? Array.isArray(Q) : $ === "object" ? Q && typeof Q == "object" && !Array.isArray(Q) : typeof Q == $ || Y && typeof Q > "u");
  }
  K3.validSchemaType = rO;
  function oO({ schema: Q, opts: X, self: Y, errSchemaPath: $ }, W, J) {
    if (Array.isArray(W.keyword) ? !W.keyword.includes(J) : W.keyword !== J) throw Error("ajv implementation error");
    let G = W.dependencies;
    if (G === null || G === void 0 ? void 0 : G.some((H) => !Object.prototype.hasOwnProperty.call(Q, H))) throw Error(`parent schema must have dependencies of ${J}: ${G.join(",")}`);
    if (W.validateSchema) {
      if (!W.validateSchema(Q[J])) {
        let B = `keyword "${J}" value is invalid at path "${$}": ` + Y.errorsText(W.validateSchema.errors);
        if (X.validateSchema === "log") Y.logger.error(B);
        else throw Error(B);
      }
    }
  }
  K3.validateKeywordUsage = oO;
});
var N3 = P((L3) => {
  Object.defineProperty(L3, "__esModule", { value: true });
  L3.extendSubschemaMode = L3.extendSubschemaData = L3.getSubschema = void 0;
  var K1 = p(), U3 = e();
  function eO(Q, { keyword: X, schemaProp: Y, schema: $, schemaPath: W, errSchemaPath: J, topSchemaRef: G }) {
    if (X !== void 0 && $ !== void 0) throw Error('both "keyword" and "schema" passed, only one allowed');
    if (X !== void 0) {
      let H = Q.schema[X];
      return Y === void 0 ? { schema: H, schemaPath: K1._`${Q.schemaPath}${(0, K1.getProperty)(X)}`, errSchemaPath: `${Q.errSchemaPath}/${X}` } : { schema: H[Y], schemaPath: K1._`${Q.schemaPath}${(0, K1.getProperty)(X)}${(0, K1.getProperty)(Y)}`, errSchemaPath: `${Q.errSchemaPath}/${X}/${(0, U3.escapeFragment)(Y)}` };
    }
    if ($ !== void 0) {
      if (W === void 0 || J === void 0 || G === void 0) throw Error('"schemaPath", "errSchemaPath" and "topSchemaRef" are required with "schema"');
      return { schema: $, schemaPath: W, topSchemaRef: G, errSchemaPath: J };
    }
    throw Error('either "keyword" or "schema" must be passed');
  }
  L3.getSubschema = eO;
  function Qw(Q, X, { dataProp: Y, dataPropType: $, data: W, dataTypes: J, propertyName: G }) {
    if (W !== void 0 && Y !== void 0) throw Error('both "data" and "dataProp" passed, only one allowed');
    let { gen: H } = X;
    if (Y !== void 0) {
      let { errorPath: z, dataPathArr: K, opts: q } = X, U = H.let("data", K1._`${X.data}${(0, K1.getProperty)(Y)}`, true);
      B(U), Q.errorPath = K1.str`${z}${(0, U3.getErrorPath)(Y, $, q.jsPropertySyntax)}`, Q.parentDataProperty = K1._`${Y}`, Q.dataPathArr = [...K, Q.parentDataProperty];
    }
    if (W !== void 0) {
      let z = W instanceof K1.Name ? W : H.let("data", W, true);
      if (B(z), G !== void 0) Q.propertyName = G;
    }
    if (J) Q.dataTypes = J;
    function B(z) {
      Q.data = z, Q.dataLevel = X.dataLevel + 1, Q.dataTypes = [], X.definedProperties = /* @__PURE__ */ new Set(), Q.parentData = X.data, Q.dataNames = [...X.dataNames, z];
    }
  }
  L3.extendSubschemaData = Qw;
  function Xw(Q, { jtdDiscriminator: X, jtdMetadata: Y, compositeRule: $, createErrors: W, allErrors: J }) {
    if ($ !== void 0) Q.compositeRule = $;
    if (W !== void 0) Q.createErrors = W;
    if (J !== void 0) Q.allErrors = J;
    Q.jtdDiscriminator = X, Q.jtdMetadata = Y;
  }
  L3.extendSubschemaMode = Xw;
});
var M$ = P(($_, D3) => {
  D3.exports = function Q(X, Y) {
    if (X === Y) return true;
    if (X && Y && typeof X == "object" && typeof Y == "object") {
      if (X.constructor !== Y.constructor) return false;
      var $, W, J;
      if (Array.isArray(X)) {
        if ($ = X.length, $ != Y.length) return false;
        for (W = $; W-- !== 0; ) if (!Q(X[W], Y[W])) return false;
        return true;
      }
      if (X.constructor === RegExp) return X.source === Y.source && X.flags === Y.flags;
      if (X.valueOf !== Object.prototype.valueOf) return X.valueOf() === Y.valueOf();
      if (X.toString !== Object.prototype.toString) return X.toString() === Y.toString();
      if (J = Object.keys(X), $ = J.length, $ !== Object.keys(Y).length) return false;
      for (W = $; W-- !== 0; ) if (!Object.prototype.hasOwnProperty.call(Y, J[W])) return false;
      for (W = $; W-- !== 0; ) {
        var G = J[W];
        if (!Q(X[G], Y[G])) return false;
      }
      return true;
    }
    return X !== X && Y !== Y;
  };
});
var w3 = P((W_, O3) => {
  var d1 = O3.exports = function(Q, X, Y) {
    if (typeof X == "function") Y = X, X = {};
    Y = X.cb || Y;
    var $ = typeof Y == "function" ? Y : Y.pre || function() {
    }, W = Y.post || function() {
    };
    k8(X, $, W, Q, "", Q);
  };
  d1.keywords = { additionalItems: true, items: true, contains: true, additionalProperties: true, propertyNames: true, not: true, if: true, then: true, else: true };
  d1.arrayKeywords = { items: true, allOf: true, anyOf: true, oneOf: true };
  d1.propsKeywords = { $defs: true, definitions: true, properties: true, patternProperties: true, dependencies: true };
  d1.skipKeywords = { default: true, enum: true, const: true, required: true, maximum: true, minimum: true, exclusiveMaximum: true, exclusiveMinimum: true, multipleOf: true, maxLength: true, minLength: true, pattern: true, format: true, maxItems: true, minItems: true, uniqueItems: true, maxProperties: true, minProperties: true };
  function k8(Q, X, Y, $, W, J, G, H, B, z) {
    if ($ && typeof $ == "object" && !Array.isArray($)) {
      X($, W, J, G, H, B, z);
      for (var K in $) {
        var q = $[K];
        if (Array.isArray(q)) {
          if (K in d1.arrayKeywords) for (var U = 0; U < q.length; U++) k8(Q, X, Y, q[U], W + "/" + K + "/" + U, J, W, K, $, U);
        } else if (K in d1.propsKeywords) {
          if (q && typeof q == "object") for (var V in q) k8(Q, X, Y, q[V], W + "/" + K + "/" + Ww(V), J, W, K, $, V);
        } else if (K in d1.keywords || Q.allKeys && !(K in d1.skipKeywords)) k8(Q, X, Y, q, W + "/" + K, J, W, K, $);
      }
      Y($, W, J, G, H, B, z);
    }
  }
  function Ww(Q) {
    return Q.replace(/~/g, "~0").replace(/\//g, "~1");
  }
});
var c4 = P((R3) => {
  Object.defineProperty(R3, "__esModule", { value: true });
  R3.getSchemaRefs = R3.resolveUrl = R3.normalizeId = R3._getFullPath = R3.getFullPath = R3.inlineRef = void 0;
  var Jw = e(), Gw = M$(), Hw = w3(), Bw = /* @__PURE__ */ new Set(["type", "format", "pattern", "maxLength", "minLength", "maxProperties", "minProperties", "maxItems", "minItems", "maximum", "minimum", "uniqueItems", "multipleOf", "required", "enum", "const"]);
  function zw(Q, X = true) {
    if (typeof Q == "boolean") return true;
    if (X === true) return !A$(Q);
    if (!X) return false;
    return M3(Q) <= X;
  }
  R3.inlineRef = zw;
  var Kw = /* @__PURE__ */ new Set(["$ref", "$recursiveRef", "$recursiveAnchor", "$dynamicRef", "$dynamicAnchor"]);
  function A$(Q) {
    for (let X in Q) {
      if (Kw.has(X)) return true;
      let Y = Q[X];
      if (Array.isArray(Y) && Y.some(A$)) return true;
      if (typeof Y == "object" && A$(Y)) return true;
    }
    return false;
  }
  function M3(Q) {
    let X = 0;
    for (let Y in Q) {
      if (Y === "$ref") return 1 / 0;
      if (X++, Bw.has(Y)) continue;
      if (typeof Q[Y] == "object") (0, Jw.eachItem)(Q[Y], ($) => X += M3($));
      if (X === 1 / 0) return 1 / 0;
    }
    return X;
  }
  function A3(Q, X = "", Y) {
    if (Y !== false) X = p6(X);
    let $ = Q.parse(X);
    return j3(Q, $);
  }
  R3.getFullPath = A3;
  function j3(Q, X) {
    return Q.serialize(X).split("#")[0] + "#";
  }
  R3._getFullPath = j3;
  var Vw = /#\/?$/;
  function p6(Q) {
    return Q ? Q.replace(Vw, "") : "";
  }
  R3.normalizeId = p6;
  function qw(Q, X, Y) {
    return Y = p6(Y), Q.resolve(X, Y);
  }
  R3.resolveUrl = qw;
  var Uw = /^[a-z_][-a-z0-9._]*$/i;
  function Lw(Q, X) {
    if (typeof Q == "boolean") return {};
    let { schemaId: Y, uriResolver: $ } = this.opts, W = p6(Q[Y] || X), J = { "": W }, G = A3($, W, false), H = {}, B = /* @__PURE__ */ new Set();
    return Hw(Q, { allKeys: true }, (q, U, V, F) => {
      if (F === void 0) return;
      let L = G + U, N = J[F];
      if (typeof q[Y] == "string") N = w.call(this, q[Y]);
      A.call(this, q.$anchor), A.call(this, q.$dynamicAnchor), J[U] = N;
      function w(R) {
        let S = this.opts.uriResolver.resolve;
        if (R = p6(N ? S(N, R) : R), B.has(R)) throw K(R);
        B.add(R);
        let C = this.refs[R];
        if (typeof C == "string") C = this.refs[C];
        if (typeof C == "object") z(q, C.schema, R);
        else if (R !== p6(L)) if (R[0] === "#") z(q, H[R], R), H[R] = q;
        else this.refs[R] = L;
        return R;
      }
      function A(R) {
        if (typeof R == "string") {
          if (!Uw.test(R)) throw Error(`invalid anchor "${R}"`);
          w.call(this, `#${R}`);
        }
      }
    }), H;
    function z(q, U, V) {
      if (U !== void 0 && !Gw(q, U)) throw K(V);
    }
    function K(q) {
      return Error(`reference "${q}" resolves to more than one schema`);
    }
  }
  R3.getSchemaRefs = Lw;
});
var i4 = P((f3) => {
  Object.defineProperty(f3, "__esModule", { value: true });
  f3.getData = f3.KeywordCxt = f3.validateFunctionCode = void 0;
  var Z3 = mG(), E3 = m4(), R$ = U$(), v8 = m4(), Mw = $3(), d4 = q3(), j$ = N3(), T = p(), u = R1(), Aw = c4(), I1 = e(), p4 = l4();
  function jw(Q) {
    if (v3(Q)) {
      if (_3(Q), k3(Q)) {
        Ew(Q);
        return;
      }
    }
    C3(Q, () => (0, Z3.topBoolOrEmptySchema)(Q));
  }
  f3.validateFunctionCode = jw;
  function C3({ gen: Q, validateName: X, schema: Y, schemaEnv: $, opts: W }, J) {
    if (W.code.es5) Q.func(X, T._`${u.default.data}, ${u.default.valCxt}`, $.$async, () => {
      Q.code(T._`"use strict"; ${b3(Y, W)}`), Iw(Q, W), Q.code(J);
    });
    else Q.func(X, T._`${u.default.data}, ${Rw(W)}`, $.$async, () => Q.code(b3(Y, W)).code(J));
  }
  function Rw(Q) {
    return T._`{${u.default.instancePath}="", ${u.default.parentData}, ${u.default.parentDataProperty}, ${u.default.rootData}=${u.default.data}${Q.dynamicRef ? T._`, ${u.default.dynamicAnchors}={}` : T.nil}}={}`;
  }
  function Iw(Q, X) {
    Q.if(u.default.valCxt, () => {
      if (Q.var(u.default.instancePath, T._`${u.default.valCxt}.${u.default.instancePath}`), Q.var(u.default.parentData, T._`${u.default.valCxt}.${u.default.parentData}`), Q.var(u.default.parentDataProperty, T._`${u.default.valCxt}.${u.default.parentDataProperty}`), Q.var(u.default.rootData, T._`${u.default.valCxt}.${u.default.rootData}`), X.dynamicRef) Q.var(u.default.dynamicAnchors, T._`${u.default.valCxt}.${u.default.dynamicAnchors}`);
    }, () => {
      if (Q.var(u.default.instancePath, T._`""`), Q.var(u.default.parentData, T._`undefined`), Q.var(u.default.parentDataProperty, T._`undefined`), Q.var(u.default.rootData, u.default.data), X.dynamicRef) Q.var(u.default.dynamicAnchors, T._`{}`);
    });
  }
  function Ew(Q) {
    let { schema: X, opts: Y, gen: $ } = Q;
    C3(Q, () => {
      if (Y.$comment && X.$comment) x3(Q);
      if (Cw(Q), $.let(u.default.vErrors, null), $.let(u.default.errors, 0), Y.unevaluated) bw(Q);
      T3(Q), _w(Q);
    });
    return;
  }
  function bw(Q) {
    let { gen: X, validateName: Y } = Q;
    Q.evaluated = X.const("evaluated", T._`${Y}.evaluated`), X.if(T._`${Q.evaluated}.dynamicProps`, () => X.assign(T._`${Q.evaluated}.props`, T._`undefined`)), X.if(T._`${Q.evaluated}.dynamicItems`, () => X.assign(T._`${Q.evaluated}.items`, T._`undefined`));
  }
  function b3(Q, X) {
    let Y = typeof Q == "object" && Q[X.schemaId];
    return Y && (X.code.source || X.code.process) ? T._`/*# sourceURL=${Y} */` : T.nil;
  }
  function Pw(Q, X) {
    if (v3(Q)) {
      if (_3(Q), k3(Q)) {
        Sw(Q, X);
        return;
      }
    }
    (0, Z3.boolOrEmptySchema)(Q, X);
  }
  function k3({ schema: Q, self: X }) {
    if (typeof Q == "boolean") return !Q;
    for (let Y in Q) if (X.RULES.all[Y]) return true;
    return false;
  }
  function v3(Q) {
    return typeof Q.schema != "boolean";
  }
  function Sw(Q, X) {
    let { schema: Y, gen: $, opts: W } = Q;
    if (W.$comment && Y.$comment) x3(Q);
    kw(Q), vw(Q);
    let J = $.const("_errs", u.default.errors);
    T3(Q, J), $.var(X, T._`${J} === ${u.default.errors}`);
  }
  function _3(Q) {
    (0, I1.checkUnknownRules)(Q), Zw(Q);
  }
  function T3(Q, X) {
    if (Q.opts.jtd) return P3(Q, [], false, X);
    let Y = (0, E3.getSchemaTypes)(Q.schema), $ = (0, E3.coerceAndCheckDataType)(Q, Y);
    P3(Q, Y, !$, X);
  }
  function Zw(Q) {
    let { schema: X, errSchemaPath: Y, opts: $, self: W } = Q;
    if (X.$ref && $.ignoreKeywordsWithRef && (0, I1.schemaHasRulesButRef)(X, W.RULES)) W.logger.warn(`$ref: keywords ignored in schema at path "${Y}"`);
  }
  function Cw(Q) {
    let { schema: X, opts: Y } = Q;
    if (X.default !== void 0 && Y.useDefaults && Y.strictSchema) (0, I1.checkStrictMode)(Q, "default is ignored in the schema root");
  }
  function kw(Q) {
    let X = Q.schema[Q.opts.schemaId];
    if (X) Q.baseId = (0, Aw.resolveUrl)(Q.opts.uriResolver, Q.baseId, X);
  }
  function vw(Q) {
    if (Q.schema.$async && !Q.schemaEnv.$async) throw Error("async schema in sync schema");
  }
  function x3({ gen: Q, schemaEnv: X, schema: Y, errSchemaPath: $, opts: W }) {
    let J = Y.$comment;
    if (W.$comment === true) Q.code(T._`${u.default.self}.logger.log(${J})`);
    else if (typeof W.$comment == "function") {
      let G = T.str`${$}/$comment`, H = Q.scopeValue("root", { ref: X.root });
      Q.code(T._`${u.default.self}.opts.$comment(${J}, ${G}, ${H}.schema)`);
    }
  }
  function _w(Q) {
    let { gen: X, schemaEnv: Y, validateName: $, ValidationError: W, opts: J } = Q;
    if (Y.$async) X.if(T._`${u.default.errors} === 0`, () => X.return(u.default.data), () => X.throw(T._`new ${W}(${u.default.vErrors})`));
    else {
      if (X.assign(T._`${$}.errors`, u.default.vErrors), J.unevaluated) Tw(Q);
      X.return(T._`${u.default.errors} === 0`);
    }
  }
  function Tw({ gen: Q, evaluated: X, props: Y, items: $ }) {
    if (Y instanceof T.Name) Q.assign(T._`${X}.props`, Y);
    if ($ instanceof T.Name) Q.assign(T._`${X}.items`, $);
  }
  function P3(Q, X, Y, $) {
    let { gen: W, schema: J, data: G, allErrors: H, opts: B, self: z } = Q, { RULES: K } = z;
    if (J.$ref && (B.ignoreKeywordsWithRef || !(0, I1.schemaHasRulesButRef)(J, K))) {
      W.block(() => g3(Q, "$ref", K.all.$ref.definition));
      return;
    }
    if (!B.jtd) xw(Q, X);
    W.block(() => {
      for (let U of K.rules) q(U);
      q(K.post);
    });
    function q(U) {
      if (!(0, R$.shouldUseGroup)(J, U)) return;
      if (U.type) {
        if (W.if((0, v8.checkDataType)(U.type, G, B.strictNumbers)), S3(Q, U), X.length === 1 && X[0] === U.type && Y) W.else(), (0, v8.reportTypeError)(Q);
        W.endIf();
      } else S3(Q, U);
      if (!H) W.if(T._`${u.default.errors} === ${$ || 0}`);
    }
  }
  function S3(Q, X) {
    let { gen: Y, schema: $, opts: { useDefaults: W } } = Q;
    if (W) (0, Mw.assignDefaults)(Q, X.type);
    Y.block(() => {
      for (let J of X.rules) if ((0, R$.shouldUseRule)($, J)) g3(Q, J.keyword, J.definition, X.type);
    });
  }
  function xw(Q, X) {
    if (Q.schemaEnv.meta || !Q.opts.strictTypes) return;
    if (yw(Q, X), !Q.opts.allowUnionTypes) gw(Q, X);
    hw(Q, Q.dataTypes);
  }
  function yw(Q, X) {
    if (!X.length) return;
    if (!Q.dataTypes.length) {
      Q.dataTypes = X;
      return;
    }
    X.forEach((Y) => {
      if (!y3(Q.dataTypes, Y)) I$(Q, `type "${Y}" not allowed by context "${Q.dataTypes.join(",")}"`);
    }), uw(Q, X);
  }
  function gw(Q, X) {
    if (X.length > 1 && !(X.length === 2 && X.includes("null"))) I$(Q, "use allowUnionTypes to allow union type keyword");
  }
  function hw(Q, X) {
    let Y = Q.self.RULES.all;
    for (let $ in Y) {
      let W = Y[$];
      if (typeof W == "object" && (0, R$.shouldUseRule)(Q.schema, W)) {
        let { type: J } = W.definition;
        if (J.length && !J.some((G) => fw(X, G))) I$(Q, `missing type "${J.join(",")}" for keyword "${$}"`);
      }
    }
  }
  function fw(Q, X) {
    return Q.includes(X) || X === "number" && Q.includes("integer");
  }
  function y3(Q, X) {
    return Q.includes(X) || X === "integer" && Q.includes("number");
  }
  function uw(Q, X) {
    let Y = [];
    for (let $ of Q.dataTypes) if (y3(X, $)) Y.push($);
    else if (X.includes("integer") && $ === "number") Y.push("integer");
    Q.dataTypes = Y;
  }
  function I$(Q, X) {
    let Y = Q.schemaEnv.baseId + Q.errSchemaPath;
    X += ` at "${Y}" (strictTypes)`, (0, I1.checkStrictMode)(Q, X, Q.opts.strictTypes);
  }
  class E$ {
    constructor(Q, X, Y) {
      if ((0, d4.validateKeywordUsage)(Q, X, Y), this.gen = Q.gen, this.allErrors = Q.allErrors, this.keyword = Y, this.data = Q.data, this.schema = Q.schema[Y], this.$data = X.$data && Q.opts.$data && this.schema && this.schema.$data, this.schemaValue = (0, I1.schemaRefOrVal)(Q, this.schema, Y, this.$data), this.schemaType = X.schemaType, this.parentSchema = Q.schema, this.params = {}, this.it = Q, this.def = X, this.$data) this.schemaCode = Q.gen.const("vSchema", h3(this.$data, Q));
      else if (this.schemaCode = this.schemaValue, !(0, d4.validSchemaType)(this.schema, X.schemaType, X.allowUndefined)) throw Error(`${Y} value must be ${JSON.stringify(X.schemaType)}`);
      if ("code" in X ? X.trackErrors : X.errors !== false) this.errsCount = Q.gen.const("_errs", u.default.errors);
    }
    result(Q, X, Y) {
      this.failResult((0, T.not)(Q), X, Y);
    }
    failResult(Q, X, Y) {
      if (this.gen.if(Q), Y) Y();
      else this.error();
      if (X) {
        if (this.gen.else(), X(), this.allErrors) this.gen.endIf();
      } else if (this.allErrors) this.gen.endIf();
      else this.gen.else();
    }
    pass(Q, X) {
      this.failResult((0, T.not)(Q), void 0, X);
    }
    fail(Q) {
      if (Q === void 0) {
        if (this.error(), !this.allErrors) this.gen.if(false);
        return;
      }
      if (this.gen.if(Q), this.error(), this.allErrors) this.gen.endIf();
      else this.gen.else();
    }
    fail$data(Q) {
      if (!this.$data) return this.fail(Q);
      let { schemaCode: X } = this;
      this.fail(T._`${X} !== undefined && (${(0, T.or)(this.invalid$data(), Q)})`);
    }
    error(Q, X, Y) {
      if (X) {
        this.setParams(X), this._error(Q, Y), this.setParams({});
        return;
      }
      this._error(Q, Y);
    }
    _error(Q, X) {
      (Q ? p4.reportExtraError : p4.reportError)(this, this.def.error, X);
    }
    $dataError() {
      (0, p4.reportError)(this, this.def.$dataError || p4.keyword$DataError);
    }
    reset() {
      if (this.errsCount === void 0) throw Error('add "trackErrors" to keyword definition');
      (0, p4.resetErrorsCount)(this.gen, this.errsCount);
    }
    ok(Q) {
      if (!this.allErrors) this.gen.if(Q);
    }
    setParams(Q, X) {
      if (X) Object.assign(this.params, Q);
      else this.params = Q;
    }
    block$data(Q, X, Y = T.nil) {
      this.gen.block(() => {
        this.check$data(Q, Y), X();
      });
    }
    check$data(Q = T.nil, X = T.nil) {
      if (!this.$data) return;
      let { gen: Y, schemaCode: $, schemaType: W, def: J } = this;
      if (Y.if((0, T.or)(T._`${$} === undefined`, X)), Q !== T.nil) Y.assign(Q, true);
      if (W.length || J.validateSchema) {
        if (Y.elseIf(this.invalid$data()), this.$dataError(), Q !== T.nil) Y.assign(Q, false);
      }
      Y.else();
    }
    invalid$data() {
      let { gen: Q, schemaCode: X, schemaType: Y, def: $, it: W } = this;
      return (0, T.or)(J(), G());
      function J() {
        if (Y.length) {
          if (!(X instanceof T.Name)) throw Error("ajv implementation error");
          let H = Array.isArray(Y) ? Y : [Y];
          return T._`${(0, v8.checkDataTypes)(H, X, W.opts.strictNumbers, v8.DataType.Wrong)}`;
        }
        return T.nil;
      }
      function G() {
        if ($.validateSchema) {
          let H = Q.scopeValue("validate$data", { ref: $.validateSchema });
          return T._`!${H}(${X})`;
        }
        return T.nil;
      }
    }
    subschema(Q, X) {
      let Y = (0, j$.getSubschema)(this.it, Q);
      (0, j$.extendSubschemaData)(Y, this.it, Q), (0, j$.extendSubschemaMode)(Y, Q);
      let $ = { ...this.it, ...Y, items: void 0, props: void 0 };
      return Pw($, X), $;
    }
    mergeEvaluated(Q, X) {
      let { it: Y, gen: $ } = this;
      if (!Y.opts.unevaluated) return;
      if (Y.props !== true && Q.props !== void 0) Y.props = I1.mergeEvaluated.props($, Q.props, Y.props, X);
      if (Y.items !== true && Q.items !== void 0) Y.items = I1.mergeEvaluated.items($, Q.items, Y.items, X);
    }
    mergeValidEvaluated(Q, X) {
      let { it: Y, gen: $ } = this;
      if (Y.opts.unevaluated && (Y.props !== true || Y.items !== true)) return $.if(X, () => this.mergeEvaluated(Q, T.Name)), true;
    }
  }
  f3.KeywordCxt = E$;
  function g3(Q, X, Y, $) {
    let W = new E$(Q, Y, X);
    if ("code" in Y) Y.code(W, $);
    else if (W.$data && Y.validate) (0, d4.funcKeywordCode)(W, Y);
    else if ("macro" in Y) (0, d4.macroKeywordCode)(W, Y);
    else if (Y.compile || Y.validate) (0, d4.funcKeywordCode)(W, Y);
  }
  var lw = /^\/(?:[^~]|~0|~1)*$/, mw = /^([0-9]+)(#|\/(?:[^~]|~0|~1)*)?$/;
  function h3(Q, { dataLevel: X, dataNames: Y, dataPathArr: $ }) {
    let W, J;
    if (Q === "") return u.default.rootData;
    if (Q[0] === "/") {
      if (!lw.test(Q)) throw Error(`Invalid JSON-pointer: ${Q}`);
      W = Q, J = u.default.rootData;
    } else {
      let z = mw.exec(Q);
      if (!z) throw Error(`Invalid JSON-pointer: ${Q}`);
      let K = +z[1];
      if (W = z[2], W === "#") {
        if (K >= X) throw Error(B("property/index", K));
        return $[X - K];
      }
      if (K > X) throw Error(B("data", K));
      if (J = Y[X - K], !W) return J;
    }
    let G = J, H = W.split("/");
    for (let z of H) if (z) J = T._`${J}${(0, T.getProperty)((0, I1.unescapeJsonPointer)(z))}`, G = T._`${G} && ${J}`;
    return G;
    function B(z, K) {
      return `Cannot access ${z} ${K} levels up, current level is ${X}`;
    }
  }
  f3.getData = h3;
});
var _8 = P((m3) => {
  Object.defineProperty(m3, "__esModule", { value: true });
  class l3 extends Error {
    constructor(Q) {
      super("validation failed");
      this.errors = Q, this.ajv = this.validation = true;
    }
  }
  m3.default = l3;
});
var n4 = P((p3) => {
  Object.defineProperty(p3, "__esModule", { value: true });
  var b$ = c4();
  class c3 extends Error {
    constructor(Q, X, Y, $) {
      super($ || `can't resolve reference ${Y} from id ${X}`);
      this.missingRef = (0, b$.resolveUrl)(Q, X, Y), this.missingSchema = (0, b$.normalizeId)((0, b$.getFullPath)(Q, this.missingRef));
    }
  }
  p3.default = c3;
});
var x8 = P((n3) => {
  Object.defineProperty(n3, "__esModule", { value: true });
  n3.resolveSchema = n3.getCompilingSchema = n3.resolveRef = n3.compileSchema = n3.SchemaEnv = void 0;
  var e0 = p(), nw = _8(), K6 = R1(), Q1 = c4(), d3 = e(), rw = i4();
  class r4 {
    constructor(Q) {
      var X;
      this.refs = {}, this.dynamicAnchors = {};
      let Y;
      if (typeof Q.schema == "object") Y = Q.schema;
      this.schema = Q.schema, this.schemaId = Q.schemaId, this.root = Q.root || this, this.baseId = (X = Q.baseId) !== null && X !== void 0 ? X : (0, Q1.normalizeId)(Y === null || Y === void 0 ? void 0 : Y[Q.schemaId || "$id"]), this.schemaPath = Q.schemaPath, this.localRefs = Q.localRefs, this.meta = Q.meta, this.$async = Y === null || Y === void 0 ? void 0 : Y.$async, this.refs = {};
    }
  }
  n3.SchemaEnv = r4;
  function S$(Q) {
    let X = i3.call(this, Q);
    if (X) return X;
    let Y = (0, Q1.getFullPath)(this.opts.uriResolver, Q.root.baseId), { es5: $, lines: W } = this.opts.code, { ownProperties: J } = this.opts, G = new e0.CodeGen(this.scope, { es5: $, lines: W, ownProperties: J }), H;
    if (Q.$async) H = G.scopeValue("Error", { ref: nw.default, code: e0._`require("ajv/dist/runtime/validation_error").default` });
    let B = G.scopeName("validate");
    Q.validateName = B;
    let z = { gen: G, allErrors: this.opts.allErrors, data: K6.default.data, parentData: K6.default.parentData, parentDataProperty: K6.default.parentDataProperty, dataNames: [K6.default.data], dataPathArr: [e0.nil], dataLevel: 0, dataTypes: [], definedProperties: /* @__PURE__ */ new Set(), topSchemaRef: G.scopeValue("schema", this.opts.code.source === true ? { ref: Q.schema, code: (0, e0.stringify)(Q.schema) } : { ref: Q.schema }), validateName: B, ValidationError: H, schema: Q.schema, schemaEnv: Q, rootId: Y, baseId: Q.baseId || Y, schemaPath: e0.nil, errSchemaPath: Q.schemaPath || (this.opts.jtd ? "" : "#"), errorPath: e0._`""`, opts: this.opts, self: this }, K;
    try {
      this._compilations.add(Q), (0, rw.validateFunctionCode)(z), G.optimize(this.opts.code.optimize);
      let q = G.toString();
      if (K = `${G.scopeRefs(K6.default.scope)}return ${q}`, this.opts.code.process) K = this.opts.code.process(K, Q);
      let V = Function(`${K6.default.self}`, `${K6.default.scope}`, K)(this, this.scope.get());
      if (this.scope.value(B, { ref: V }), V.errors = null, V.schema = Q.schema, V.schemaEnv = Q, Q.$async) V.$async = true;
      if (this.opts.code.source === true) V.source = { validateName: B, validateCode: q, scopeValues: G._values };
      if (this.opts.unevaluated) {
        let { props: F, items: L } = z;
        if (V.evaluated = { props: F instanceof e0.Name ? void 0 : F, items: L instanceof e0.Name ? void 0 : L, dynamicProps: F instanceof e0.Name, dynamicItems: L instanceof e0.Name }, V.source) V.source.evaluated = (0, e0.stringify)(V.evaluated);
      }
      return Q.validate = V, Q;
    } catch (q) {
      if (delete Q.validate, delete Q.validateName, K) this.logger.error("Error compiling schema, function code:", K);
      throw q;
    } finally {
      this._compilations.delete(Q);
    }
  }
  n3.compileSchema = S$;
  function ow(Q, X, Y) {
    var $;
    Y = (0, Q1.resolveUrl)(this.opts.uriResolver, X, Y);
    let W = Q.refs[Y];
    if (W) return W;
    let J = sw.call(this, Q, Y);
    if (J === void 0) {
      let G = ($ = Q.localRefs) === null || $ === void 0 ? void 0 : $[Y], { schemaId: H } = this.opts;
      if (G) J = new r4({ schema: G, schemaId: H, root: Q, baseId: X });
    }
    if (J === void 0) return;
    return Q.refs[Y] = tw.call(this, J);
  }
  n3.resolveRef = ow;
  function tw(Q) {
    if ((0, Q1.inlineRef)(Q.schema, this.opts.inlineRefs)) return Q.schema;
    return Q.validate ? Q : S$.call(this, Q);
  }
  function i3(Q) {
    for (let X of this._compilations) if (aw(X, Q)) return X;
  }
  n3.getCompilingSchema = i3;
  function aw(Q, X) {
    return Q.schema === X.schema && Q.root === X.root && Q.baseId === X.baseId;
  }
  function sw(Q, X) {
    let Y;
    while (typeof (Y = this.refs[X]) == "string") X = Y;
    return Y || this.schemas[X] || T8.call(this, Q, X);
  }
  function T8(Q, X) {
    let Y = this.opts.uriResolver.parse(X), $ = (0, Q1._getFullPath)(this.opts.uriResolver, Y), W = (0, Q1.getFullPath)(this.opts.uriResolver, Q.baseId, void 0);
    if (Object.keys(Q.schema).length > 0 && $ === W) return P$.call(this, Y, Q);
    let J = (0, Q1.normalizeId)($), G = this.refs[J] || this.schemas[J];
    if (typeof G == "string") {
      let H = T8.call(this, Q, G);
      if (typeof (H === null || H === void 0 ? void 0 : H.schema) !== "object") return;
      return P$.call(this, Y, H);
    }
    if (typeof (G === null || G === void 0 ? void 0 : G.schema) !== "object") return;
    if (!G.validate) S$.call(this, G);
    if (J === (0, Q1.normalizeId)(X)) {
      let { schema: H } = G, { schemaId: B } = this.opts, z = H[B];
      if (z) W = (0, Q1.resolveUrl)(this.opts.uriResolver, W, z);
      return new r4({ schema: H, schemaId: B, root: Q, baseId: W });
    }
    return P$.call(this, Y, G);
  }
  n3.resolveSchema = T8;
  var ew = /* @__PURE__ */ new Set(["properties", "patternProperties", "enum", "dependencies", "definitions"]);
  function P$(Q, { baseId: X, schema: Y, root: $ }) {
    var W;
    if (((W = Q.fragment) === null || W === void 0 ? void 0 : W[0]) !== "/") return;
    for (let H of Q.fragment.slice(1).split("/")) {
      if (typeof Y === "boolean") return;
      let B = Y[(0, d3.unescapeFragment)(H)];
      if (B === void 0) return;
      Y = B;
      let z = typeof Y === "object" && Y[this.opts.schemaId];
      if (!ew.has(H) && z) X = (0, Q1.resolveUrl)(this.opts.uriResolver, X, z);
    }
    let J;
    if (typeof Y != "boolean" && Y.$ref && !(0, d3.schemaHasRulesButRef)(Y, this.RULES)) {
      let H = (0, Q1.resolveUrl)(this.opts.uriResolver, X, Y.$ref);
      J = T8.call(this, $, H);
    }
    let { schemaId: G } = this.opts;
    if (J = J || new r4({ schema: Y, schemaId: G, root: $, baseId: X }), J.schema !== J.root.schema) return J;
    return;
  }
});
var o3 = P((K_, WM) => {
  WM.exports = { $id: "https://raw.githubusercontent.com/ajv-validator/ajv/master/lib/refs/data.json#", description: "Meta-schema for $data reference (JSON AnySchema extension proposal)", type: "object", required: ["$data"], properties: { $data: { type: "string", anyOf: [{ format: "relative-json-pointer" }, { format: "json-pointer" }] } }, additionalProperties: false };
});
var a3 = P((V_, t3) => {
  var JM = { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7, 8: 8, 9: 9, a: 10, A: 10, b: 11, B: 11, c: 12, C: 12, d: 13, D: 13, e: 14, E: 14, f: 15, F: 15 };
  t3.exports = { HEX: JM };
});
var JH = P((q_, WH) => {
  var { HEX: GM } = a3(), HM = /^(?:(?:25[0-5]|2[0-4]\d|1\d{2}|[1-9]\d|\d)\.){3}(?:25[0-5]|2[0-4]\d|1\d{2}|[1-9]\d|\d)$/u;
  function XH(Q) {
    if ($H(Q, ".") < 3) return { host: Q, isIPV4: false };
    let X = Q.match(HM) || [], [Y] = X;
    if (Y) return { host: zM(Y, "."), isIPV4: true };
    else return { host: Q, isIPV4: false };
  }
  function Z$(Q, X = false) {
    let Y = "", $ = true;
    for (let W of Q) {
      if (GM[W] === void 0) return;
      if (W !== "0" && $ === true) $ = false;
      if (!$) Y += W;
    }
    if (X && Y.length === 0) Y = "0";
    return Y;
  }
  function BM(Q) {
    let X = 0, Y = { error: false, address: "", zone: "" }, $ = [], W = [], J = false, G = false, H = false;
    function B() {
      if (W.length) {
        if (J === false) {
          let z = Z$(W);
          if (z !== void 0) $.push(z);
          else return Y.error = true, false;
        }
        W.length = 0;
      }
      return true;
    }
    for (let z = 0; z < Q.length; z++) {
      let K = Q[z];
      if (K === "[" || K === "]") continue;
      if (K === ":") {
        if (G === true) H = true;
        if (!B()) break;
        if (X++, $.push(":"), X > 7) {
          Y.error = true;
          break;
        }
        if (z - 1 >= 0 && Q[z - 1] === ":") G = true;
        continue;
      } else if (K === "%") {
        if (!B()) break;
        J = true;
      } else {
        W.push(K);
        continue;
      }
    }
    if (W.length) if (J) Y.zone = W.join("");
    else if (H) $.push(W.join(""));
    else $.push(Z$(W));
    return Y.address = $.join(""), Y;
  }
  function YH(Q) {
    if ($H(Q, ":") < 2) return { host: Q, isIPV6: false };
    let X = BM(Q);
    if (!X.error) {
      let { address: Y, address: $ } = X;
      if (X.zone) Y += "%" + X.zone, $ += "%25" + X.zone;
      return { host: Y, escapedHost: $, isIPV6: true };
    } else return { host: Q, isIPV6: false };
  }
  function zM(Q, X) {
    let Y = "", $ = true, W = Q.length;
    for (let J = 0; J < W; J++) {
      let G = Q[J];
      if (G === "0" && $) {
        if (J + 1 <= W && Q[J + 1] === X || J + 1 === W) Y += G, $ = false;
      } else {
        if (G === X) $ = true;
        else $ = false;
        Y += G;
      }
    }
    return Y;
  }
  function $H(Q, X) {
    let Y = 0;
    for (let $ = 0; $ < Q.length; $++) if (Q[$] === X) Y++;
    return Y;
  }
  var s3 = /^\.\.?\//u, e3 = /^\/\.(?:\/|$)/u, QH = /^\/\.\.(?:\/|$)/u, KM = /^\/?(?:.|\n)*?(?=\/|$)/u;
  function VM(Q) {
    let X = [];
    while (Q.length) if (Q.match(s3)) Q = Q.replace(s3, "");
    else if (Q.match(e3)) Q = Q.replace(e3, "/");
    else if (Q.match(QH)) Q = Q.replace(QH, "/"), X.pop();
    else if (Q === "." || Q === "..") Q = "";
    else {
      let Y = Q.match(KM);
      if (Y) {
        let $ = Y[0];
        Q = Q.slice($.length), X.push($);
      } else throw Error("Unexpected dot segment condition");
    }
    return X.join("");
  }
  function qM(Q, X) {
    let Y = X !== true ? escape : unescape;
    if (Q.scheme !== void 0) Q.scheme = Y(Q.scheme);
    if (Q.userinfo !== void 0) Q.userinfo = Y(Q.userinfo);
    if (Q.host !== void 0) Q.host = Y(Q.host);
    if (Q.path !== void 0) Q.path = Y(Q.path);
    if (Q.query !== void 0) Q.query = Y(Q.query);
    if (Q.fragment !== void 0) Q.fragment = Y(Q.fragment);
    return Q;
  }
  function UM(Q) {
    let X = [];
    if (Q.userinfo !== void 0) X.push(Q.userinfo), X.push("@");
    if (Q.host !== void 0) {
      let Y = unescape(Q.host), $ = XH(Y);
      if ($.isIPV4) Y = $.host;
      else {
        let W = YH($.host);
        if (W.isIPV6 === true) Y = `[${W.escapedHost}]`;
        else Y = Q.host;
      }
      X.push(Y);
    }
    if (typeof Q.port === "number" || typeof Q.port === "string") X.push(":"), X.push(String(Q.port));
    return X.length ? X.join("") : void 0;
  }
  WH.exports = { recomposeAuthority: UM, normalizeComponentEncoding: qM, removeDotSegments: VM, normalizeIPv4: XH, normalizeIPv6: YH, stringArrayToHexStripped: Z$ };
});
var VH = P((U_, KH) => {
  var LM = /^[\da-f]{8}-[\da-f]{4}-[\da-f]{4}-[\da-f]{4}-[\da-f]{12}$/iu, FM = /([\da-z][\d\-a-z]{0,31}):((?:[\w!$'()*+,\-.:;=@]|%[\da-f]{2})+)/iu;
  function GH(Q) {
    return typeof Q.secure === "boolean" ? Q.secure : String(Q.scheme).toLowerCase() === "wss";
  }
  function HH(Q) {
    if (!Q.host) Q.error = Q.error || "HTTP URIs must have a host.";
    return Q;
  }
  function BH(Q) {
    let X = String(Q.scheme).toLowerCase() === "https";
    if (Q.port === (X ? 443 : 80) || Q.port === "") Q.port = void 0;
    if (!Q.path) Q.path = "/";
    return Q;
  }
  function NM(Q) {
    return Q.secure = GH(Q), Q.resourceName = (Q.path || "/") + (Q.query ? "?" + Q.query : ""), Q.path = void 0, Q.query = void 0, Q;
  }
  function DM(Q) {
    if (Q.port === (GH(Q) ? 443 : 80) || Q.port === "") Q.port = void 0;
    if (typeof Q.secure === "boolean") Q.scheme = Q.secure ? "wss" : "ws", Q.secure = void 0;
    if (Q.resourceName) {
      let [X, Y] = Q.resourceName.split("?");
      Q.path = X && X !== "/" ? X : void 0, Q.query = Y, Q.resourceName = void 0;
    }
    return Q.fragment = void 0, Q;
  }
  function OM(Q, X) {
    if (!Q.path) return Q.error = "URN can not be parsed", Q;
    let Y = Q.path.match(FM);
    if (Y) {
      let $ = X.scheme || Q.scheme || "urn";
      Q.nid = Y[1].toLowerCase(), Q.nss = Y[2];
      let W = `${$}:${X.nid || Q.nid}`, J = C$[W];
      if (Q.path = void 0, J) Q = J.parse(Q, X);
    } else Q.error = Q.error || "URN can not be parsed.";
    return Q;
  }
  function wM(Q, X) {
    let Y = X.scheme || Q.scheme || "urn", $ = Q.nid.toLowerCase(), W = `${Y}:${X.nid || $}`, J = C$[W];
    if (J) Q = J.serialize(Q, X);
    let G = Q, H = Q.nss;
    return G.path = `${$ || X.nid}:${H}`, X.skipEscape = true, G;
  }
  function MM(Q, X) {
    let Y = Q;
    if (Y.uuid = Y.nss, Y.nss = void 0, !X.tolerant && (!Y.uuid || !LM.test(Y.uuid))) Y.error = Y.error || "UUID is not valid.";
    return Y;
  }
  function AM(Q) {
    let X = Q;
    return X.nss = (Q.uuid || "").toLowerCase(), X;
  }
  var zH = { scheme: "http", domainHost: true, parse: HH, serialize: BH }, jM = { scheme: "https", domainHost: zH.domainHost, parse: HH, serialize: BH }, y8 = { scheme: "ws", domainHost: true, parse: NM, serialize: DM }, RM = { scheme: "wss", domainHost: y8.domainHost, parse: y8.parse, serialize: y8.serialize }, IM = { scheme: "urn", parse: OM, serialize: wM, skipNormalize: true }, EM = { scheme: "urn:uuid", parse: MM, serialize: AM, skipNormalize: true }, C$ = { http: zH, https: jM, ws: y8, wss: RM, urn: IM, "urn:uuid": EM };
  KH.exports = C$;
});
var UH = P((L_, h8) => {
  var { normalizeIPv6: bM, normalizeIPv4: PM, removeDotSegments: o4, recomposeAuthority: SM, normalizeComponentEncoding: g8 } = JH(), k$ = VH();
  function ZM(Q, X) {
    if (typeof Q === "string") Q = V1(E1(Q, X), X);
    else if (typeof Q === "object") Q = E1(V1(Q, X), X);
    return Q;
  }
  function CM(Q, X, Y) {
    let $ = Object.assign({ scheme: "null" }, Y), W = qH(E1(Q, $), E1(X, $), $, true);
    return V1(W, { ...$, skipEscape: true });
  }
  function qH(Q, X, Y, $) {
    let W = {};
    if (!$) Q = E1(V1(Q, Y), Y), X = E1(V1(X, Y), Y);
    if (Y = Y || {}, !Y.tolerant && X.scheme) W.scheme = X.scheme, W.userinfo = X.userinfo, W.host = X.host, W.port = X.port, W.path = o4(X.path || ""), W.query = X.query;
    else {
      if (X.userinfo !== void 0 || X.host !== void 0 || X.port !== void 0) W.userinfo = X.userinfo, W.host = X.host, W.port = X.port, W.path = o4(X.path || ""), W.query = X.query;
      else {
        if (!X.path) if (W.path = Q.path, X.query !== void 0) W.query = X.query;
        else W.query = Q.query;
        else {
          if (X.path.charAt(0) === "/") W.path = o4(X.path);
          else {
            if ((Q.userinfo !== void 0 || Q.host !== void 0 || Q.port !== void 0) && !Q.path) W.path = "/" + X.path;
            else if (!Q.path) W.path = X.path;
            else W.path = Q.path.slice(0, Q.path.lastIndexOf("/") + 1) + X.path;
            W.path = o4(W.path);
          }
          W.query = X.query;
        }
        W.userinfo = Q.userinfo, W.host = Q.host, W.port = Q.port;
      }
      W.scheme = Q.scheme;
    }
    return W.fragment = X.fragment, W;
  }
  function kM(Q, X, Y) {
    if (typeof Q === "string") Q = unescape(Q), Q = V1(g8(E1(Q, Y), true), { ...Y, skipEscape: true });
    else if (typeof Q === "object") Q = V1(g8(Q, true), { ...Y, skipEscape: true });
    if (typeof X === "string") X = unescape(X), X = V1(g8(E1(X, Y), true), { ...Y, skipEscape: true });
    else if (typeof X === "object") X = V1(g8(X, true), { ...Y, skipEscape: true });
    return Q.toLowerCase() === X.toLowerCase();
  }
  function V1(Q, X) {
    let Y = { host: Q.host, scheme: Q.scheme, userinfo: Q.userinfo, port: Q.port, path: Q.path, query: Q.query, nid: Q.nid, nss: Q.nss, uuid: Q.uuid, fragment: Q.fragment, reference: Q.reference, resourceName: Q.resourceName, secure: Q.secure, error: "" }, $ = Object.assign({}, X), W = [], J = k$[($.scheme || Y.scheme || "").toLowerCase()];
    if (J && J.serialize) J.serialize(Y, $);
    if (Y.path !== void 0) if (!$.skipEscape) {
      if (Y.path = escape(Y.path), Y.scheme !== void 0) Y.path = Y.path.split("%3A").join(":");
    } else Y.path = unescape(Y.path);
    if ($.reference !== "suffix" && Y.scheme) W.push(Y.scheme, ":");
    let G = SM(Y);
    if (G !== void 0) {
      if ($.reference !== "suffix") W.push("//");
      if (W.push(G), Y.path && Y.path.charAt(0) !== "/") W.push("/");
    }
    if (Y.path !== void 0) {
      let H = Y.path;
      if (!$.absolutePath && (!J || !J.absolutePath)) H = o4(H);
      if (G === void 0) H = H.replace(/^\/\//u, "/%2F");
      W.push(H);
    }
    if (Y.query !== void 0) W.push("?", Y.query);
    if (Y.fragment !== void 0) W.push("#", Y.fragment);
    return W.join("");
  }
  var vM = Array.from({ length: 127 }, (Q, X) => /[^!"$&'()*+,\-.;=_`a-z{}~]/u.test(String.fromCharCode(X)));
  function _M(Q) {
    let X = 0;
    for (let Y = 0, $ = Q.length; Y < $; ++Y) if (X = Q.charCodeAt(Y), X > 126 || vM[X]) return true;
    return false;
  }
  var TM = /^(?:([^#/:?]+):)?(?:\/\/((?:([^#/?@]*)@)?(\[[^#/?\]]+\]|[^#/:?]*)(?::(\d*))?))?([^#?]*)(?:\?([^#]*))?(?:#((?:.|[\n\r])*))?/u;
  function E1(Q, X) {
    let Y = Object.assign({}, X), $ = { scheme: void 0, userinfo: void 0, host: "", port: void 0, path: "", query: void 0, fragment: void 0 }, W = Q.indexOf("%") !== -1, J = false;
    if (Y.reference === "suffix") Q = (Y.scheme ? Y.scheme + ":" : "") + "//" + Q;
    let G = Q.match(TM);
    if (G) {
      if ($.scheme = G[1], $.userinfo = G[3], $.host = G[4], $.port = parseInt(G[5], 10), $.path = G[6] || "", $.query = G[7], $.fragment = G[8], isNaN($.port)) $.port = G[5];
      if ($.host) {
        let B = PM($.host);
        if (B.isIPV4 === false) {
          let z = bM(B.host);
          $.host = z.host.toLowerCase(), J = z.isIPV6;
        } else $.host = B.host, J = true;
      }
      if ($.scheme === void 0 && $.userinfo === void 0 && $.host === void 0 && $.port === void 0 && $.query === void 0 && !$.path) $.reference = "same-document";
      else if ($.scheme === void 0) $.reference = "relative";
      else if ($.fragment === void 0) $.reference = "absolute";
      else $.reference = "uri";
      if (Y.reference && Y.reference !== "suffix" && Y.reference !== $.reference) $.error = $.error || "URI is not a " + Y.reference + " reference.";
      let H = k$[(Y.scheme || $.scheme || "").toLowerCase()];
      if (!Y.unicodeSupport && (!H || !H.unicodeSupport)) {
        if ($.host && (Y.domainHost || H && H.domainHost) && J === false && _M($.host)) try {
          $.host = URL.domainToASCII($.host.toLowerCase());
        } catch (B) {
          $.error = $.error || "Host's domain name can not be converted to ASCII: " + B;
        }
      }
      if (!H || H && !H.skipNormalize) {
        if (W && $.scheme !== void 0) $.scheme = unescape($.scheme);
        if (W && $.host !== void 0) $.host = unescape($.host);
        if ($.path) $.path = escape(unescape($.path));
        if ($.fragment) $.fragment = encodeURI(decodeURIComponent($.fragment));
      }
      if (H && H.parse) H.parse($, Y);
    } else $.error = $.error || "URI can not be parsed.";
    return $;
  }
  var v$ = { SCHEMES: k$, normalize: ZM, resolve: CM, resolveComponents: qH, equal: kM, serialize: V1, parse: E1 };
  h8.exports = v$;
  h8.exports.default = v$;
  h8.exports.fastUri = v$;
});
var NH = P((FH) => {
  Object.defineProperty(FH, "__esModule", { value: true });
  var LH = UH();
  LH.code = 'require("ajv/dist/runtime/uri").default';
  FH.default = LH;
});
var IH = P((b1) => {
  Object.defineProperty(b1, "__esModule", { value: true });
  b1.CodeGen = b1.Name = b1.nil = b1.stringify = b1.str = b1._ = b1.KeywordCxt = void 0;
  var yM = i4();
  Object.defineProperty(b1, "KeywordCxt", { enumerable: true, get: function() {
    return yM.KeywordCxt;
  } });
  var d6 = p();
  Object.defineProperty(b1, "_", { enumerable: true, get: function() {
    return d6._;
  } });
  Object.defineProperty(b1, "str", { enumerable: true, get: function() {
    return d6.str;
  } });
  Object.defineProperty(b1, "stringify", { enumerable: true, get: function() {
    return d6.stringify;
  } });
  Object.defineProperty(b1, "nil", { enumerable: true, get: function() {
    return d6.nil;
  } });
  Object.defineProperty(b1, "Name", { enumerable: true, get: function() {
    return d6.Name;
  } });
  Object.defineProperty(b1, "CodeGen", { enumerable: true, get: function() {
    return d6.CodeGen;
  } });
  var gM = _8(), AH = n4(), hM = q$(), t4 = x8(), fM = p(), a4 = c4(), f8 = m4(), T$ = e(), DH = o3(), uM = NH(), jH = (Q, X) => new RegExp(Q, X);
  jH.code = "new RegExp";
  var lM = ["removeAdditional", "useDefaults", "coerceTypes"], mM = /* @__PURE__ */ new Set(["validate", "serialize", "parse", "wrapper", "root", "schema", "keyword", "pattern", "formats", "validate$data", "func", "obj", "Error"]), cM = { errorDataPath: "", format: "`validateFormats: false` can be used instead.", nullable: '"nullable" keyword is supported by default.', jsonPointers: "Deprecated jsPropertySyntax can be used instead.", extendRefs: "Deprecated ignoreKeywordsWithRef can be used instead.", missingRefs: "Pass empty schema with $id that should be ignored to ajv.addSchema.", processCode: "Use option `code: {process: (code, schemaEnv: object) => string}`", sourceCode: "Use option `code: {source: true}`", strictDefaults: "It is default now, see option `strict`.", strictKeywords: "It is default now, see option `strict`.", uniqueItems: '"uniqueItems" keyword is always validated.', unknownFormats: "Disable strict mode or pass `true` to `ajv.addFormat` (or `formats` option).", cache: "Map is used as cache, schema object as key.", serialize: "Map is used as cache, schema object as key.", ajvErrors: "It is default now." }, pM = { ignoreKeywordsWithRef: "", jsPropertySyntax: "", unicode: '"minLength"/"maxLength" account for unicode characters by default.' }, OH = 200;
  function dM(Q) {
    var X, Y, $, W, J, G, H, B, z, K, q, U, V, F, L, N, w, A, R, S, C, K0, V0, s, O0;
    let L0 = Q.strict, U1 = (X = Q.code) === null || X === void 0 ? void 0 : X.optimize, P1 = U1 === true || U1 === void 0 ? 1 : U1 || 0, o1 = ($ = (Y = Q.code) === null || Y === void 0 ? void 0 : Y.regExp) !== null && $ !== void 0 ? $ : jH, m = (W = Q.uriResolver) !== null && W !== void 0 ? W : uM.default;
    return { strictSchema: (G = (J = Q.strictSchema) !== null && J !== void 0 ? J : L0) !== null && G !== void 0 ? G : true, strictNumbers: (B = (H = Q.strictNumbers) !== null && H !== void 0 ? H : L0) !== null && B !== void 0 ? B : true, strictTypes: (K = (z = Q.strictTypes) !== null && z !== void 0 ? z : L0) !== null && K !== void 0 ? K : "log", strictTuples: (U = (q = Q.strictTuples) !== null && q !== void 0 ? q : L0) !== null && U !== void 0 ? U : "log", strictRequired: (F = (V = Q.strictRequired) !== null && V !== void 0 ? V : L0) !== null && F !== void 0 ? F : false, code: Q.code ? { ...Q.code, optimize: P1, regExp: o1 } : { optimize: P1, regExp: o1 }, loopRequired: (L = Q.loopRequired) !== null && L !== void 0 ? L : OH, loopEnum: (N = Q.loopEnum) !== null && N !== void 0 ? N : OH, meta: (w = Q.meta) !== null && w !== void 0 ? w : true, messages: (A = Q.messages) !== null && A !== void 0 ? A : true, inlineRefs: (R = Q.inlineRefs) !== null && R !== void 0 ? R : true, schemaId: (S = Q.schemaId) !== null && S !== void 0 ? S : "$id", addUsedSchema: (C = Q.addUsedSchema) !== null && C !== void 0 ? C : true, validateSchema: (K0 = Q.validateSchema) !== null && K0 !== void 0 ? K0 : true, validateFormats: (V0 = Q.validateFormats) !== null && V0 !== void 0 ? V0 : true, unicodeRegExp: (s = Q.unicodeRegExp) !== null && s !== void 0 ? s : true, int32range: (O0 = Q.int32range) !== null && O0 !== void 0 ? O0 : true, uriResolver: m };
  }
  class u8 {
    constructor(Q = {}) {
      this.schemas = {}, this.refs = {}, this.formats = {}, this._compilations = /* @__PURE__ */ new Set(), this._loading = {}, this._cache = /* @__PURE__ */ new Map(), Q = this.opts = { ...Q, ...dM(Q) };
      let { es5: X, lines: Y } = this.opts.code;
      this.scope = new fM.ValueScope({ scope: {}, prefixes: mM, es5: X, lines: Y }), this.logger = aM(Q.logger);
      let $ = Q.validateFormats;
      if (Q.validateFormats = false, this.RULES = (0, hM.getRules)(), wH.call(this, cM, Q, "NOT SUPPORTED"), wH.call(this, pM, Q, "DEPRECATED", "warn"), this._metaOpts = oM.call(this), Q.formats) nM.call(this);
      if (this._addVocabularies(), this._addDefaultMetaSchema(), Q.keywords) rM.call(this, Q.keywords);
      if (typeof Q.meta == "object") this.addMetaSchema(Q.meta);
      iM.call(this), Q.validateFormats = $;
    }
    _addVocabularies() {
      this.addKeyword("$async");
    }
    _addDefaultMetaSchema() {
      let { $data: Q, meta: X, schemaId: Y } = this.opts, $ = DH;
      if (Y === "id") $ = { ...DH }, $.id = $.$id, delete $.$id;
      if (X && Q) this.addMetaSchema($, $[Y], false);
    }
    defaultMeta() {
      let { meta: Q, schemaId: X } = this.opts;
      return this.opts.defaultMeta = typeof Q == "object" ? Q[X] || Q : void 0;
    }
    validate(Q, X) {
      let Y;
      if (typeof Q == "string") {
        if (Y = this.getSchema(Q), !Y) throw Error(`no schema with key or ref "${Q}"`);
      } else Y = this.compile(Q);
      let $ = Y(X);
      if (!("$async" in Y)) this.errors = Y.errors;
      return $;
    }
    compile(Q, X) {
      let Y = this._addSchema(Q, X);
      return Y.validate || this._compileSchemaEnv(Y);
    }
    compileAsync(Q, X) {
      if (typeof this.opts.loadSchema != "function") throw Error("options.loadSchema should be a function");
      let { loadSchema: Y } = this.opts;
      return $.call(this, Q, X);
      async function $(z, K) {
        await W.call(this, z.$schema);
        let q = this._addSchema(z, K);
        return q.validate || J.call(this, q);
      }
      async function W(z) {
        if (z && !this.getSchema(z)) await $.call(this, { $ref: z }, true);
      }
      async function J(z) {
        try {
          return this._compileSchemaEnv(z);
        } catch (K) {
          if (!(K instanceof AH.default)) throw K;
          return G.call(this, K), await H.call(this, K.missingSchema), J.call(this, z);
        }
      }
      function G({ missingSchema: z, missingRef: K }) {
        if (this.refs[z]) throw Error(`AnySchema ${z} is loaded but ${K} cannot be resolved`);
      }
      async function H(z) {
        let K = await B.call(this, z);
        if (!this.refs[z]) await W.call(this, K.$schema);
        if (!this.refs[z]) this.addSchema(K, z, X);
      }
      async function B(z) {
        let K = this._loading[z];
        if (K) return K;
        try {
          return await (this._loading[z] = Y(z));
        } finally {
          delete this._loading[z];
        }
      }
    }
    addSchema(Q, X, Y, $ = this.opts.validateSchema) {
      if (Array.isArray(Q)) {
        for (let J of Q) this.addSchema(J, void 0, Y, $);
        return this;
      }
      let W;
      if (typeof Q === "object") {
        let { schemaId: J } = this.opts;
        if (W = Q[J], W !== void 0 && typeof W != "string") throw Error(`schema ${J} must be string`);
      }
      return X = (0, a4.normalizeId)(X || W), this._checkUnique(X), this.schemas[X] = this._addSchema(Q, Y, X, $, true), this;
    }
    addMetaSchema(Q, X, Y = this.opts.validateSchema) {
      return this.addSchema(Q, X, true, Y), this;
    }
    validateSchema(Q, X) {
      if (typeof Q == "boolean") return true;
      let Y;
      if (Y = Q.$schema, Y !== void 0 && typeof Y != "string") throw Error("$schema must be a string");
      if (Y = Y || this.opts.defaultMeta || this.defaultMeta(), !Y) return this.logger.warn("meta-schema not available"), this.errors = null, true;
      let $ = this.validate(Y, Q);
      if (!$ && X) {
        let W = "schema is invalid: " + this.errorsText();
        if (this.opts.validateSchema === "log") this.logger.error(W);
        else throw Error(W);
      }
      return $;
    }
    getSchema(Q) {
      let X;
      while (typeof (X = MH.call(this, Q)) == "string") Q = X;
      if (X === void 0) {
        let { schemaId: Y } = this.opts, $ = new t4.SchemaEnv({ schema: {}, schemaId: Y });
        if (X = t4.resolveSchema.call(this, $, Q), !X) return;
        this.refs[Q] = X;
      }
      return X.validate || this._compileSchemaEnv(X);
    }
    removeSchema(Q) {
      if (Q instanceof RegExp) return this._removeAllSchemas(this.schemas, Q), this._removeAllSchemas(this.refs, Q), this;
      switch (typeof Q) {
        case "undefined":
          return this._removeAllSchemas(this.schemas), this._removeAllSchemas(this.refs), this._cache.clear(), this;
        case "string": {
          let X = MH.call(this, Q);
          if (typeof X == "object") this._cache.delete(X.schema);
          return delete this.schemas[Q], delete this.refs[Q], this;
        }
        case "object": {
          let X = Q;
          this._cache.delete(X);
          let Y = Q[this.opts.schemaId];
          if (Y) Y = (0, a4.normalizeId)(Y), delete this.schemas[Y], delete this.refs[Y];
          return this;
        }
        default:
          throw Error("ajv.removeSchema: invalid parameter");
      }
    }
    addVocabulary(Q) {
      for (let X of Q) this.addKeyword(X);
      return this;
    }
    addKeyword(Q, X) {
      let Y;
      if (typeof Q == "string") {
        if (Y = Q, typeof X == "object") this.logger.warn("these parameters are deprecated, see docs for addKeyword"), X.keyword = Y;
      } else if (typeof Q == "object" && X === void 0) {
        if (X = Q, Y = X.keyword, Array.isArray(Y) && !Y.length) throw Error("addKeywords: keyword must be string or non-empty array");
      } else throw Error("invalid addKeywords parameters");
      if (eM.call(this, Y, X), !X) return (0, T$.eachItem)(Y, (W) => _$.call(this, W)), this;
      XA.call(this, X);
      let $ = { ...X, type: (0, f8.getJSONTypes)(X.type), schemaType: (0, f8.getJSONTypes)(X.schemaType) };
      return (0, T$.eachItem)(Y, $.type.length === 0 ? (W) => _$.call(this, W, $) : (W) => $.type.forEach((J) => _$.call(this, W, $, J))), this;
    }
    getKeyword(Q) {
      let X = this.RULES.all[Q];
      return typeof X == "object" ? X.definition : !!X;
    }
    removeKeyword(Q) {
      let { RULES: X } = this;
      delete X.keywords[Q], delete X.all[Q];
      for (let Y of X.rules) {
        let $ = Y.rules.findIndex((W) => W.keyword === Q);
        if ($ >= 0) Y.rules.splice($, 1);
      }
      return this;
    }
    addFormat(Q, X) {
      if (typeof X == "string") X = new RegExp(X);
      return this.formats[Q] = X, this;
    }
    errorsText(Q = this.errors, { separator: X = ", ", dataVar: Y = "data" } = {}) {
      if (!Q || Q.length === 0) return "No errors";
      return Q.map(($) => `${Y}${$.instancePath} ${$.message}`).reduce(($, W) => $ + X + W);
    }
    $dataMetaSchema(Q, X) {
      let Y = this.RULES.all;
      Q = JSON.parse(JSON.stringify(Q));
      for (let $ of X) {
        let W = $.split("/").slice(1), J = Q;
        for (let G of W) J = J[G];
        for (let G in Y) {
          let H = Y[G];
          if (typeof H != "object") continue;
          let { $data: B } = H.definition, z = J[G];
          if (B && z) J[G] = RH(z);
        }
      }
      return Q;
    }
    _removeAllSchemas(Q, X) {
      for (let Y in Q) {
        let $ = Q[Y];
        if (!X || X.test(Y)) {
          if (typeof $ == "string") delete Q[Y];
          else if ($ && !$.meta) this._cache.delete($.schema), delete Q[Y];
        }
      }
    }
    _addSchema(Q, X, Y, $ = this.opts.validateSchema, W = this.opts.addUsedSchema) {
      let J, { schemaId: G } = this.opts;
      if (typeof Q == "object") J = Q[G];
      else if (this.opts.jtd) throw Error("schema must be object");
      else if (typeof Q != "boolean") throw Error("schema must be object or boolean");
      let H = this._cache.get(Q);
      if (H !== void 0) return H;
      Y = (0, a4.normalizeId)(J || Y);
      let B = a4.getSchemaRefs.call(this, Q, Y);
      if (H = new t4.SchemaEnv({ schema: Q, schemaId: G, meta: X, baseId: Y, localRefs: B }), this._cache.set(H.schema, H), W && !Y.startsWith("#")) {
        if (Y) this._checkUnique(Y);
        this.refs[Y] = H;
      }
      if ($) this.validateSchema(Q, true);
      return H;
    }
    _checkUnique(Q) {
      if (this.schemas[Q] || this.refs[Q]) throw Error(`schema with key or id "${Q}" already exists`);
    }
    _compileSchemaEnv(Q) {
      if (Q.meta) this._compileMetaSchema(Q);
      else t4.compileSchema.call(this, Q);
      if (!Q.validate) throw Error("ajv implementation error");
      return Q.validate;
    }
    _compileMetaSchema(Q) {
      let X = this.opts;
      this.opts = this._metaOpts;
      try {
        t4.compileSchema.call(this, Q);
      } finally {
        this.opts = X;
      }
    }
  }
  u8.ValidationError = gM.default;
  u8.MissingRefError = AH.default;
  b1.default = u8;
  function wH(Q, X, Y, $ = "error") {
    for (let W in Q) {
      let J = W;
      if (J in X) this.logger[$](`${Y}: option ${W}. ${Q[J]}`);
    }
  }
  function MH(Q) {
    return Q = (0, a4.normalizeId)(Q), this.schemas[Q] || this.refs[Q];
  }
  function iM() {
    let Q = this.opts.schemas;
    if (!Q) return;
    if (Array.isArray(Q)) this.addSchema(Q);
    else for (let X in Q) this.addSchema(Q[X], X);
  }
  function nM() {
    for (let Q in this.opts.formats) {
      let X = this.opts.formats[Q];
      if (X) this.addFormat(Q, X);
    }
  }
  function rM(Q) {
    if (Array.isArray(Q)) {
      this.addVocabulary(Q);
      return;
    }
    this.logger.warn("keywords option as map is deprecated, pass array");
    for (let X in Q) {
      let Y = Q[X];
      if (!Y.keyword) Y.keyword = X;
      this.addKeyword(Y);
    }
  }
  function oM() {
    let Q = { ...this.opts };
    for (let X of lM) delete Q[X];
    return Q;
  }
  var tM = { log() {
  }, warn() {
  }, error() {
  } };
  function aM(Q) {
    if (Q === false) return tM;
    if (Q === void 0) return console;
    if (Q.log && Q.warn && Q.error) return Q;
    throw Error("logger must implement log, warn and error methods");
  }
  var sM = /^[a-z_$][a-z0-9_$:-]*$/i;
  function eM(Q, X) {
    let { RULES: Y } = this;
    if ((0, T$.eachItem)(Q, ($) => {
      if (Y.keywords[$]) throw Error(`Keyword ${$} is already defined`);
      if (!sM.test($)) throw Error(`Keyword ${$} has invalid name`);
    }), !X) return;
    if (X.$data && !("code" in X || "validate" in X)) throw Error('$data keyword must have "code" or "validate" function');
  }
  function _$(Q, X, Y) {
    var $;
    let W = X === null || X === void 0 ? void 0 : X.post;
    if (Y && W) throw Error('keyword with "post" flag cannot have "type"');
    let { RULES: J } = this, G = W ? J.post : J.rules.find(({ type: B }) => B === Y);
    if (!G) G = { type: Y, rules: [] }, J.rules.push(G);
    if (J.keywords[Q] = true, !X) return;
    let H = { keyword: Q, definition: { ...X, type: (0, f8.getJSONTypes)(X.type), schemaType: (0, f8.getJSONTypes)(X.schemaType) } };
    if (X.before) QA.call(this, G, H, X.before);
    else G.rules.push(H);
    J.all[Q] = H, ($ = X.implements) === null || $ === void 0 || $.forEach((B) => this.addKeyword(B));
  }
  function QA(Q, X, Y) {
    let $ = Q.rules.findIndex((W) => W.keyword === Y);
    if ($ >= 0) Q.rules.splice($, 0, X);
    else Q.rules.push(X), this.logger.warn(`rule ${Y} is not defined`);
  }
  function XA(Q) {
    let { metaSchema: X } = Q;
    if (X === void 0) return;
    if (Q.$data && this.opts.$data) X = RH(X);
    Q.validateSchema = this.compile(X, true);
  }
  var YA = { $ref: "https://raw.githubusercontent.com/ajv-validator/ajv/master/lib/refs/data.json#" };
  function RH(Q) {
    return { anyOf: [Q, YA] };
  }
});
var bH = P((EH) => {
  Object.defineProperty(EH, "__esModule", { value: true });
  var JA = { keyword: "id", code() {
    throw Error('NOT SUPPORTED: keyword "id", use "$id" for schema ID');
  } };
  EH.default = JA;
});
var vH = P((CH) => {
  Object.defineProperty(CH, "__esModule", { value: true });
  CH.callRef = CH.getValidate = void 0;
  var HA = n4(), PH = d0(), g0 = p(), i6 = R1(), SH = x8(), l8 = e(), BA = { keyword: "$ref", schemaType: "string", code(Q) {
    let { gen: X, schema: Y, it: $ } = Q, { baseId: W, schemaEnv: J, validateName: G, opts: H, self: B } = $, { root: z } = J;
    if ((Y === "#" || Y === "#/") && W === z.baseId) return q();
    let K = SH.resolveRef.call(B, z, W, Y);
    if (K === void 0) throw new HA.default($.opts.uriResolver, W, Y);
    if (K instanceof SH.SchemaEnv) return U(K);
    return V(K);
    function q() {
      if (J === z) return m8(Q, G, J, J.$async);
      let F = X.scopeValue("root", { ref: z });
      return m8(Q, g0._`${F}.validate`, z, z.$async);
    }
    function U(F) {
      let L = ZH(Q, F);
      m8(Q, L, F, F.$async);
    }
    function V(F) {
      let L = X.scopeValue("schema", H.code.source === true ? { ref: F, code: (0, g0.stringify)(F) } : { ref: F }), N = X.name("valid"), w = Q.subschema({ schema: F, dataTypes: [], schemaPath: g0.nil, topSchemaRef: L, errSchemaPath: Y }, N);
      Q.mergeEvaluated(w), Q.ok(N);
    }
  } };
  function ZH(Q, X) {
    let { gen: Y } = Q;
    return X.validate ? Y.scopeValue("validate", { ref: X.validate }) : g0._`${Y.scopeValue("wrapper", { ref: X })}.validate`;
  }
  CH.getValidate = ZH;
  function m8(Q, X, Y, $) {
    let { gen: W, it: J } = Q, { allErrors: G, schemaEnv: H, opts: B } = J, z = B.passContext ? i6.default.this : g0.nil;
    if ($) K();
    else q();
    function K() {
      if (!H.$async) throw Error("async schema referenced by sync schema");
      let F = W.let("valid");
      W.try(() => {
        if (W.code(g0._`await ${(0, PH.callValidateCode)(Q, X, z)}`), V(X), !G) W.assign(F, true);
      }, (L) => {
        if (W.if(g0._`!(${L} instanceof ${J.ValidationError})`, () => W.throw(L)), U(L), !G) W.assign(F, false);
      }), Q.ok(F);
    }
    function q() {
      Q.result((0, PH.callValidateCode)(Q, X, z), () => V(X), () => U(X));
    }
    function U(F) {
      let L = g0._`${F}.errors`;
      W.assign(i6.default.vErrors, g0._`${i6.default.vErrors} === null ? ${L} : ${i6.default.vErrors}.concat(${L})`), W.assign(i6.default.errors, g0._`${i6.default.vErrors}.length`);
    }
    function V(F) {
      var L;
      if (!J.opts.unevaluated) return;
      let N = (L = Y === null || Y === void 0 ? void 0 : Y.validate) === null || L === void 0 ? void 0 : L.evaluated;
      if (J.props !== true) if (N && !N.dynamicProps) {
        if (N.props !== void 0) J.props = l8.mergeEvaluated.props(W, N.props, J.props);
      } else {
        let w = W.var("props", g0._`${F}.evaluated.props`);
        J.props = l8.mergeEvaluated.props(W, w, J.props, g0.Name);
      }
      if (J.items !== true) if (N && !N.dynamicItems) {
        if (N.items !== void 0) J.items = l8.mergeEvaluated.items(W, N.items, J.items);
      } else {
        let w = W.var("items", g0._`${F}.evaluated.items`);
        J.items = l8.mergeEvaluated.items(W, w, J.items, g0.Name);
      }
    }
  }
  CH.callRef = m8;
  CH.default = BA;
});
var TH = P((_H) => {
  Object.defineProperty(_H, "__esModule", { value: true });
  var VA = bH(), qA = vH(), UA = ["$schema", "$id", "$defs", "$vocabulary", { keyword: "$comment" }, "definitions", VA.default, qA.default];
  _H.default = UA;
});
var yH = P((xH) => {
  Object.defineProperty(xH, "__esModule", { value: true });
  var c8 = p(), i1 = c8.operators, p8 = { maximum: { okStr: "<=", ok: i1.LTE, fail: i1.GT }, minimum: { okStr: ">=", ok: i1.GTE, fail: i1.LT }, exclusiveMaximum: { okStr: "<", ok: i1.LT, fail: i1.GTE }, exclusiveMinimum: { okStr: ">", ok: i1.GT, fail: i1.LTE } }, FA = { message: ({ keyword: Q, schemaCode: X }) => c8.str`must be ${p8[Q].okStr} ${X}`, params: ({ keyword: Q, schemaCode: X }) => c8._`{comparison: ${p8[Q].okStr}, limit: ${X}}` }, NA = { keyword: Object.keys(p8), type: "number", schemaType: "number", $data: true, error: FA, code(Q) {
    let { keyword: X, data: Y, schemaCode: $ } = Q;
    Q.fail$data(c8._`${Y} ${p8[X].fail} ${$} || isNaN(${Y})`);
  } };
  xH.default = NA;
});
var hH = P((gH) => {
  Object.defineProperty(gH, "__esModule", { value: true });
  var s4 = p(), OA = { message: ({ schemaCode: Q }) => s4.str`must be multiple of ${Q}`, params: ({ schemaCode: Q }) => s4._`{multipleOf: ${Q}}` }, wA = { keyword: "multipleOf", type: "number", schemaType: "number", $data: true, error: OA, code(Q) {
    let { gen: X, data: Y, schemaCode: $, it: W } = Q, J = W.opts.multipleOfPrecision, G = X.let("res"), H = J ? s4._`Math.abs(Math.round(${G}) - ${G}) > 1e-${J}` : s4._`${G} !== parseInt(${G})`;
    Q.fail$data(s4._`(${$} === 0 || (${G} = ${Y}/${$}, ${H}))`);
  } };
  gH.default = wA;
});
var lH = P((uH) => {
  Object.defineProperty(uH, "__esModule", { value: true });
  function fH(Q) {
    let X = Q.length, Y = 0, $ = 0, W;
    while ($ < X) if (Y++, W = Q.charCodeAt($++), W >= 55296 && W <= 56319 && $ < X) {
      if (W = Q.charCodeAt($), (W & 64512) === 56320) $++;
    }
    return Y;
  }
  uH.default = fH;
  fH.code = 'require("ajv/dist/runtime/ucs2length").default';
});
var cH = P((mH) => {
  Object.defineProperty(mH, "__esModule", { value: true });
  var V6 = p(), jA = e(), RA = lH(), IA = { message({ keyword: Q, schemaCode: X }) {
    let Y = Q === "maxLength" ? "more" : "fewer";
    return V6.str`must NOT have ${Y} than ${X} characters`;
  }, params: ({ schemaCode: Q }) => V6._`{limit: ${Q}}` }, EA = { keyword: ["maxLength", "minLength"], type: "string", schemaType: "number", $data: true, error: IA, code(Q) {
    let { keyword: X, data: Y, schemaCode: $, it: W } = Q, J = X === "maxLength" ? V6.operators.GT : V6.operators.LT, G = W.opts.unicode === false ? V6._`${Y}.length` : V6._`${(0, jA.useFunc)(Q.gen, RA.default)}(${Y})`;
    Q.fail$data(V6._`${G} ${J} ${$}`);
  } };
  mH.default = EA;
});
var dH = P((pH) => {
  Object.defineProperty(pH, "__esModule", { value: true });
  var PA = d0(), d8 = p(), SA = { message: ({ schemaCode: Q }) => d8.str`must match pattern "${Q}"`, params: ({ schemaCode: Q }) => d8._`{pattern: ${Q}}` }, ZA = { keyword: "pattern", type: "string", schemaType: "string", $data: true, error: SA, code(Q) {
    let { data: X, $data: Y, schema: $, schemaCode: W, it: J } = Q, G = J.opts.unicodeRegExp ? "u" : "", H = Y ? d8._`(new RegExp(${W}, ${G}))` : (0, PA.usePattern)(Q, $);
    Q.fail$data(d8._`!${H}.test(${X})`);
  } };
  pH.default = ZA;
});
var nH = P((iH) => {
  Object.defineProperty(iH, "__esModule", { value: true });
  var e4 = p(), kA = { message({ keyword: Q, schemaCode: X }) {
    let Y = Q === "maxProperties" ? "more" : "fewer";
    return e4.str`must NOT have ${Y} than ${X} properties`;
  }, params: ({ schemaCode: Q }) => e4._`{limit: ${Q}}` }, vA = { keyword: ["maxProperties", "minProperties"], type: "object", schemaType: "number", $data: true, error: kA, code(Q) {
    let { keyword: X, data: Y, schemaCode: $ } = Q, W = X === "maxProperties" ? e4.operators.GT : e4.operators.LT;
    Q.fail$data(e4._`Object.keys(${Y}).length ${W} ${$}`);
  } };
  iH.default = vA;
});
var oH = P((rH) => {
  Object.defineProperty(rH, "__esModule", { value: true });
  var Q9 = d0(), X9 = p(), TA = e(), xA = { message: ({ params: { missingProperty: Q } }) => X9.str`must have required property '${Q}'`, params: ({ params: { missingProperty: Q } }) => X9._`{missingProperty: ${Q}}` }, yA = { keyword: "required", type: "object", schemaType: "array", $data: true, error: xA, code(Q) {
    let { gen: X, schema: Y, schemaCode: $, data: W, $data: J, it: G } = Q, { opts: H } = G;
    if (!J && Y.length === 0) return;
    let B = Y.length >= H.loopRequired;
    if (G.allErrors) z();
    else K();
    if (H.strictRequired) {
      let V = Q.parentSchema.properties, { definedProperties: F } = Q.it;
      for (let L of Y) if ((V === null || V === void 0 ? void 0 : V[L]) === void 0 && !F.has(L)) {
        let N = G.schemaEnv.baseId + G.errSchemaPath, w = `required property "${L}" is not defined at "${N}" (strictRequired)`;
        (0, TA.checkStrictMode)(G, w, G.opts.strictRequired);
      }
    }
    function z() {
      if (B || J) Q.block$data(X9.nil, q);
      else for (let V of Y) (0, Q9.checkReportMissingProp)(Q, V);
    }
    function K() {
      let V = X.let("missing");
      if (B || J) {
        let F = X.let("valid", true);
        Q.block$data(F, () => U(V, F)), Q.ok(F);
      } else X.if((0, Q9.checkMissingProp)(Q, Y, V)), (0, Q9.reportMissingProp)(Q, V), X.else();
    }
    function q() {
      X.forOf("prop", $, (V) => {
        Q.setParams({ missingProperty: V }), X.if((0, Q9.noPropertyInData)(X, W, V, H.ownProperties), () => Q.error());
      });
    }
    function U(V, F) {
      Q.setParams({ missingProperty: V }), X.forOf(V, $, () => {
        X.assign(F, (0, Q9.propertyInData)(X, W, V, H.ownProperties)), X.if((0, X9.not)(F), () => {
          Q.error(), X.break();
        });
      }, X9.nil);
    }
  } };
  rH.default = yA;
});
var aH = P((tH) => {
  Object.defineProperty(tH, "__esModule", { value: true });
  var Y9 = p(), hA = { message({ keyword: Q, schemaCode: X }) {
    let Y = Q === "maxItems" ? "more" : "fewer";
    return Y9.str`must NOT have ${Y} than ${X} items`;
  }, params: ({ schemaCode: Q }) => Y9._`{limit: ${Q}}` }, fA = { keyword: ["maxItems", "minItems"], type: "array", schemaType: "number", $data: true, error: hA, code(Q) {
    let { keyword: X, data: Y, schemaCode: $ } = Q, W = X === "maxItems" ? Y9.operators.GT : Y9.operators.LT;
    Q.fail$data(Y9._`${Y}.length ${W} ${$}`);
  } };
  tH.default = fA;
});
var i8 = P((eH) => {
  Object.defineProperty(eH, "__esModule", { value: true });
  var sH = M$();
  sH.code = 'require("ajv/dist/runtime/equal").default';
  eH.default = sH;
});
var XB = P((QB) => {
  Object.defineProperty(QB, "__esModule", { value: true });
  var x$ = m4(), I0 = p(), mA = e(), cA = i8(), pA = { message: ({ params: { i: Q, j: X } }) => I0.str`must NOT have duplicate items (items ## ${X} and ${Q} are identical)`, params: ({ params: { i: Q, j: X } }) => I0._`{i: ${Q}, j: ${X}}` }, dA = { keyword: "uniqueItems", type: "array", schemaType: "boolean", $data: true, error: pA, code(Q) {
    let { gen: X, data: Y, $data: $, schema: W, parentSchema: J, schemaCode: G, it: H } = Q;
    if (!$ && !W) return;
    let B = X.let("valid"), z = J.items ? (0, x$.getSchemaTypes)(J.items) : [];
    Q.block$data(B, K, I0._`${G} === false`), Q.ok(B);
    function K() {
      let F = X.let("i", I0._`${Y}.length`), L = X.let("j");
      Q.setParams({ i: F, j: L }), X.assign(B, true), X.if(I0._`${F} > 1`, () => (q() ? U : V)(F, L));
    }
    function q() {
      return z.length > 0 && !z.some((F) => F === "object" || F === "array");
    }
    function U(F, L) {
      let N = X.name("item"), w = (0, x$.checkDataTypes)(z, N, H.opts.strictNumbers, x$.DataType.Wrong), A = X.const("indices", I0._`{}`);
      X.for(I0._`;${F}--;`, () => {
        if (X.let(N, I0._`${Y}[${F}]`), X.if(w, I0._`continue`), z.length > 1) X.if(I0._`typeof ${N} == "string"`, I0._`${N} += "_"`);
        X.if(I0._`typeof ${A}[${N}] == "number"`, () => {
          X.assign(L, I0._`${A}[${N}]`), Q.error(), X.assign(B, false).break();
        }).code(I0._`${A}[${N}] = ${F}`);
      });
    }
    function V(F, L) {
      let N = (0, mA.useFunc)(X, cA.default), w = X.name("outer");
      X.label(w).for(I0._`;${F}--;`, () => X.for(I0._`${L} = ${F}; ${L}--;`, () => X.if(I0._`${N}(${Y}[${F}], ${Y}[${L}])`, () => {
        Q.error(), X.assign(B, false).break(w);
      })));
    }
  } };
  QB.default = dA;
});
var $B = P((YB) => {
  Object.defineProperty(YB, "__esModule", { value: true });
  var y$ = p(), nA = e(), rA = i8(), oA = { message: "must be equal to constant", params: ({ schemaCode: Q }) => y$._`{allowedValue: ${Q}}` }, tA = { keyword: "const", $data: true, error: oA, code(Q) {
    let { gen: X, data: Y, $data: $, schemaCode: W, schema: J } = Q;
    if ($ || J && typeof J == "object") Q.fail$data(y$._`!${(0, nA.useFunc)(X, rA.default)}(${Y}, ${W})`);
    else Q.fail(y$._`${J} !== ${Y}`);
  } };
  YB.default = tA;
});
var JB = P((WB) => {
  Object.defineProperty(WB, "__esModule", { value: true });
  var $9 = p(), sA = e(), eA = i8(), Qj = { message: "must be equal to one of the allowed values", params: ({ schemaCode: Q }) => $9._`{allowedValues: ${Q}}` }, Xj = { keyword: "enum", schemaType: "array", $data: true, error: Qj, code(Q) {
    let { gen: X, data: Y, $data: $, schema: W, schemaCode: J, it: G } = Q;
    if (!$ && W.length === 0) throw Error("enum must have non-empty array");
    let H = W.length >= G.opts.loopEnum, B, z = () => B !== null && B !== void 0 ? B : B = (0, sA.useFunc)(X, eA.default), K;
    if (H || $) K = X.let("valid"), Q.block$data(K, q);
    else {
      if (!Array.isArray(W)) throw Error("ajv implementation error");
      let V = X.const("vSchema", J);
      K = (0, $9.or)(...W.map((F, L) => U(V, L)));
    }
    Q.pass(K);
    function q() {
      X.assign(K, false), X.forOf("v", J, (V) => X.if($9._`${z()}(${Y}, ${V})`, () => X.assign(K, true).break()));
    }
    function U(V, F) {
      let L = W[F];
      return typeof L === "object" && L !== null ? $9._`${z()}(${Y}, ${V}[${F}])` : $9._`${Y} === ${L}`;
    }
  } };
  WB.default = Xj;
});
var HB = P((GB) => {
  Object.defineProperty(GB, "__esModule", { value: true });
  var $j = yH(), Wj = hH(), Jj = cH(), Gj = dH(), Hj = nH(), Bj = oH(), zj = aH(), Kj = XB(), Vj = $B(), qj = JB(), Uj = [$j.default, Wj.default, Jj.default, Gj.default, Hj.default, Bj.default, zj.default, Kj.default, { keyword: "type", schemaType: ["string", "array"] }, { keyword: "nullable", schemaType: "boolean" }, Vj.default, qj.default];
  GB.default = Uj;
});
var h$ = P((zB) => {
  Object.defineProperty(zB, "__esModule", { value: true });
  zB.validateAdditionalItems = void 0;
  var q6 = p(), g$ = e(), Fj = { message: ({ params: { len: Q } }) => q6.str`must NOT have more than ${Q} items`, params: ({ params: { len: Q } }) => q6._`{limit: ${Q}}` }, Nj = { keyword: "additionalItems", type: "array", schemaType: ["boolean", "object"], before: "uniqueItems", error: Fj, code(Q) {
    let { parentSchema: X, it: Y } = Q, { items: $ } = X;
    if (!Array.isArray($)) {
      (0, g$.checkStrictMode)(Y, '"additionalItems" is ignored when "items" is not an array of schemas');
      return;
    }
    BB(Q, $);
  } };
  function BB(Q, X) {
    let { gen: Y, schema: $, data: W, keyword: J, it: G } = Q;
    G.items = true;
    let H = Y.const("len", q6._`${W}.length`);
    if ($ === false) Q.setParams({ len: X.length }), Q.pass(q6._`${H} <= ${X.length}`);
    else if (typeof $ == "object" && !(0, g$.alwaysValidSchema)(G, $)) {
      let z = Y.var("valid", q6._`${H} <= ${X.length}`);
      Y.if((0, q6.not)(z), () => B(z)), Q.ok(z);
    }
    function B(z) {
      Y.forRange("i", X.length, H, (K) => {
        if (Q.subschema({ keyword: J, dataProp: K, dataPropType: g$.Type.Num }, z), !G.allErrors) Y.if((0, q6.not)(z), () => Y.break());
      });
    }
  }
  zB.validateAdditionalItems = BB;
  zB.default = Nj;
});
var f$ = P((UB) => {
  Object.defineProperty(UB, "__esModule", { value: true });
  UB.validateTuple = void 0;
  var VB = p(), n8 = e(), Oj = d0(), wj = { keyword: "items", type: "array", schemaType: ["object", "array", "boolean"], before: "uniqueItems", code(Q) {
    let { schema: X, it: Y } = Q;
    if (Array.isArray(X)) return qB(Q, "additionalItems", X);
    if (Y.items = true, (0, n8.alwaysValidSchema)(Y, X)) return;
    Q.ok((0, Oj.validateArray)(Q));
  } };
  function qB(Q, X, Y = Q.schema) {
    let { gen: $, parentSchema: W, data: J, keyword: G, it: H } = Q;
    if (K(W), H.opts.unevaluated && Y.length && H.items !== true) H.items = n8.mergeEvaluated.items($, Y.length, H.items);
    let B = $.name("valid"), z = $.const("len", VB._`${J}.length`);
    Y.forEach((q, U) => {
      if ((0, n8.alwaysValidSchema)(H, q)) return;
      $.if(VB._`${z} > ${U}`, () => Q.subschema({ keyword: G, schemaProp: U, dataProp: U }, B)), Q.ok(B);
    });
    function K(q) {
      let { opts: U, errSchemaPath: V } = H, F = Y.length, L = F === q.minItems && (F === q.maxItems || q[X] === false);
      if (U.strictTuples && !L) {
        let N = `"${G}" is ${F}-tuple, but minItems or maxItems/${X} are not specified or different at path "${V}"`;
        (0, n8.checkStrictMode)(H, N, U.strictTuples);
      }
    }
  }
  UB.validateTuple = qB;
  UB.default = wj;
});
var NB = P((FB) => {
  Object.defineProperty(FB, "__esModule", { value: true });
  var Aj = f$(), jj = { keyword: "prefixItems", type: "array", schemaType: ["array"], before: "uniqueItems", code: (Q) => (0, Aj.validateTuple)(Q, "items") };
  FB.default = jj;
});
var wB = P((OB) => {
  Object.defineProperty(OB, "__esModule", { value: true });
  var DB = p(), Ij = e(), Ej = d0(), bj = h$(), Pj = { message: ({ params: { len: Q } }) => DB.str`must NOT have more than ${Q} items`, params: ({ params: { len: Q } }) => DB._`{limit: ${Q}}` }, Sj = { keyword: "items", type: "array", schemaType: ["object", "boolean"], before: "uniqueItems", error: Pj, code(Q) {
    let { schema: X, parentSchema: Y, it: $ } = Q, { prefixItems: W } = Y;
    if ($.items = true, (0, Ij.alwaysValidSchema)($, X)) return;
    if (W) (0, bj.validateAdditionalItems)(Q, W);
    else Q.ok((0, Ej.validateArray)(Q));
  } };
  OB.default = Sj;
});
var AB = P((MB) => {
  Object.defineProperty(MB, "__esModule", { value: true });
  var i0 = p(), r8 = e(), Cj = { message: ({ params: { min: Q, max: X } }) => X === void 0 ? i0.str`must contain at least ${Q} valid item(s)` : i0.str`must contain at least ${Q} and no more than ${X} valid item(s)`, params: ({ params: { min: Q, max: X } }) => X === void 0 ? i0._`{minContains: ${Q}}` : i0._`{minContains: ${Q}, maxContains: ${X}}` }, kj = { keyword: "contains", type: "array", schemaType: ["object", "boolean"], before: "uniqueItems", trackErrors: true, error: Cj, code(Q) {
    let { gen: X, schema: Y, parentSchema: $, data: W, it: J } = Q, G, H, { minContains: B, maxContains: z } = $;
    if (J.opts.next) G = B === void 0 ? 1 : B, H = z;
    else G = 1;
    let K = X.const("len", i0._`${W}.length`);
    if (Q.setParams({ min: G, max: H }), H === void 0 && G === 0) {
      (0, r8.checkStrictMode)(J, '"minContains" == 0 without "maxContains": "contains" keyword ignored');
      return;
    }
    if (H !== void 0 && G > H) {
      (0, r8.checkStrictMode)(J, '"minContains" > "maxContains" is always invalid'), Q.fail();
      return;
    }
    if ((0, r8.alwaysValidSchema)(J, Y)) {
      let L = i0._`${K} >= ${G}`;
      if (H !== void 0) L = i0._`${L} && ${K} <= ${H}`;
      Q.pass(L);
      return;
    }
    J.items = true;
    let q = X.name("valid");
    if (H === void 0 && G === 1) V(q, () => X.if(q, () => X.break()));
    else if (G === 0) {
      if (X.let(q, true), H !== void 0) X.if(i0._`${W}.length > 0`, U);
    } else X.let(q, false), U();
    Q.result(q, () => Q.reset());
    function U() {
      let L = X.name("_valid"), N = X.let("count", 0);
      V(L, () => X.if(L, () => F(N)));
    }
    function V(L, N) {
      X.forRange("i", 0, K, (w) => {
        Q.subschema({ keyword: "contains", dataProp: w, dataPropType: r8.Type.Num, compositeRule: true }, L), N();
      });
    }
    function F(L) {
      if (X.code(i0._`${L}++`), H === void 0) X.if(i0._`${L} >= ${G}`, () => X.assign(q, true).break());
      else if (X.if(i0._`${L} > ${H}`, () => X.assign(q, false).break()), G === 1) X.assign(q, true);
      else X.if(i0._`${L} >= ${G}`, () => X.assign(q, true));
    }
  } };
  MB.default = kj;
});
var PB = P((IB) => {
  Object.defineProperty(IB, "__esModule", { value: true });
  IB.validateSchemaDeps = IB.validatePropertyDeps = IB.error = void 0;
  var u$ = p(), _j = e(), W9 = d0();
  IB.error = { message: ({ params: { property: Q, depsCount: X, deps: Y } }) => {
    let $ = X === 1 ? "property" : "properties";
    return u$.str`must have ${$} ${Y} when property ${Q} is present`;
  }, params: ({ params: { property: Q, depsCount: X, deps: Y, missingProperty: $ } }) => u$._`{property: ${Q},
    missingProperty: ${$},
    depsCount: ${X},
    deps: ${Y}}` };
  var Tj = { keyword: "dependencies", type: "object", schemaType: "object", error: IB.error, code(Q) {
    let [X, Y] = xj(Q);
    jB(Q, X), RB(Q, Y);
  } };
  function xj({ schema: Q }) {
    let X = {}, Y = {};
    for (let $ in Q) {
      if ($ === "__proto__") continue;
      let W = Array.isArray(Q[$]) ? X : Y;
      W[$] = Q[$];
    }
    return [X, Y];
  }
  function jB(Q, X = Q.schema) {
    let { gen: Y, data: $, it: W } = Q;
    if (Object.keys(X).length === 0) return;
    let J = Y.let("missing");
    for (let G in X) {
      let H = X[G];
      if (H.length === 0) continue;
      let B = (0, W9.propertyInData)(Y, $, G, W.opts.ownProperties);
      if (Q.setParams({ property: G, depsCount: H.length, deps: H.join(", ") }), W.allErrors) Y.if(B, () => {
        for (let z of H) (0, W9.checkReportMissingProp)(Q, z);
      });
      else Y.if(u$._`${B} && (${(0, W9.checkMissingProp)(Q, H, J)})`), (0, W9.reportMissingProp)(Q, J), Y.else();
    }
  }
  IB.validatePropertyDeps = jB;
  function RB(Q, X = Q.schema) {
    let { gen: Y, data: $, keyword: W, it: J } = Q, G = Y.name("valid");
    for (let H in X) {
      if ((0, _j.alwaysValidSchema)(J, X[H])) continue;
      Y.if((0, W9.propertyInData)(Y, $, H, J.opts.ownProperties), () => {
        let B = Q.subschema({ keyword: W, schemaProp: H }, G);
        Q.mergeValidEvaluated(B, G);
      }, () => Y.var(G, true)), Q.ok(G);
    }
  }
  IB.validateSchemaDeps = RB;
  IB.default = Tj;
});
var CB = P((ZB) => {
  Object.defineProperty(ZB, "__esModule", { value: true });
  var SB = p(), hj = e(), fj = { message: "property name must be valid", params: ({ params: Q }) => SB._`{propertyName: ${Q.propertyName}}` }, uj = { keyword: "propertyNames", type: "object", schemaType: ["object", "boolean"], error: fj, code(Q) {
    let { gen: X, schema: Y, data: $, it: W } = Q;
    if ((0, hj.alwaysValidSchema)(W, Y)) return;
    let J = X.name("valid");
    X.forIn("key", $, (G) => {
      Q.setParams({ propertyName: G }), Q.subschema({ keyword: "propertyNames", data: G, dataTypes: ["string"], propertyName: G, compositeRule: true }, J), X.if((0, SB.not)(J), () => {
        if (Q.error(true), !W.allErrors) X.break();
      });
    }), Q.ok(J);
  } };
  ZB.default = uj;
});
var l$ = P((kB) => {
  Object.defineProperty(kB, "__esModule", { value: true });
  var o8 = d0(), X1 = p(), mj = R1(), t8 = e(), cj = { message: "must NOT have additional properties", params: ({ params: Q }) => X1._`{additionalProperty: ${Q.additionalProperty}}` }, pj = { keyword: "additionalProperties", type: ["object"], schemaType: ["boolean", "object"], allowUndefined: true, trackErrors: true, error: cj, code(Q) {
    let { gen: X, schema: Y, parentSchema: $, data: W, errsCount: J, it: G } = Q;
    if (!J) throw Error("ajv implementation error");
    let { allErrors: H, opts: B } = G;
    if (G.props = true, B.removeAdditional !== "all" && (0, t8.alwaysValidSchema)(G, Y)) return;
    let z = (0, o8.allSchemaProperties)($.properties), K = (0, o8.allSchemaProperties)($.patternProperties);
    q(), Q.ok(X1._`${J} === ${mj.default.errors}`);
    function q() {
      X.forIn("key", W, (N) => {
        if (!z.length && !K.length) F(N);
        else X.if(U(N), () => F(N));
      });
    }
    function U(N) {
      let w;
      if (z.length > 8) {
        let A = (0, t8.schemaRefOrVal)(G, $.properties, "properties");
        w = (0, o8.isOwnProperty)(X, A, N);
      } else if (z.length) w = (0, X1.or)(...z.map((A) => X1._`${N} === ${A}`));
      else w = X1.nil;
      if (K.length) w = (0, X1.or)(w, ...K.map((A) => X1._`${(0, o8.usePattern)(Q, A)}.test(${N})`));
      return (0, X1.not)(w);
    }
    function V(N) {
      X.code(X1._`delete ${W}[${N}]`);
    }
    function F(N) {
      if (B.removeAdditional === "all" || B.removeAdditional && Y === false) {
        V(N);
        return;
      }
      if (Y === false) {
        if (Q.setParams({ additionalProperty: N }), Q.error(), !H) X.break();
        return;
      }
      if (typeof Y == "object" && !(0, t8.alwaysValidSchema)(G, Y)) {
        let w = X.name("valid");
        if (B.removeAdditional === "failing") L(N, w, false), X.if((0, X1.not)(w), () => {
          Q.reset(), V(N);
        });
        else if (L(N, w), !H) X.if((0, X1.not)(w), () => X.break());
      }
    }
    function L(N, w, A) {
      let R = { keyword: "additionalProperties", dataProp: N, dataPropType: t8.Type.Str };
      if (A === false) Object.assign(R, { compositeRule: true, createErrors: false, allErrors: false });
      Q.subschema(R, w);
    }
  } };
  kB.default = pj;
});
var xB = P((TB) => {
  Object.defineProperty(TB, "__esModule", { value: true });
  var ij = i4(), vB = d0(), m$ = e(), _B = l$(), nj = { keyword: "properties", type: "object", schemaType: "object", code(Q) {
    let { gen: X, schema: Y, parentSchema: $, data: W, it: J } = Q;
    if (J.opts.removeAdditional === "all" && $.additionalProperties === void 0) _B.default.code(new ij.KeywordCxt(J, _B.default, "additionalProperties"));
    let G = (0, vB.allSchemaProperties)(Y);
    for (let q of G) J.definedProperties.add(q);
    if (J.opts.unevaluated && G.length && J.props !== true) J.props = m$.mergeEvaluated.props(X, (0, m$.toHash)(G), J.props);
    let H = G.filter((q) => !(0, m$.alwaysValidSchema)(J, Y[q]));
    if (H.length === 0) return;
    let B = X.name("valid");
    for (let q of H) {
      if (z(q)) K(q);
      else {
        if (X.if((0, vB.propertyInData)(X, W, q, J.opts.ownProperties)), K(q), !J.allErrors) X.else().var(B, true);
        X.endIf();
      }
      Q.it.definedProperties.add(q), Q.ok(B);
    }
    function z(q) {
      return J.opts.useDefaults && !J.compositeRule && Y[q].default !== void 0;
    }
    function K(q) {
      Q.subschema({ keyword: "properties", schemaProp: q, dataProp: q }, B);
    }
  } };
  TB.default = nj;
});
var uB = P((fB) => {
  Object.defineProperty(fB, "__esModule", { value: true });
  var yB = d0(), a8 = p(), gB = e(), hB = e(), oj = { keyword: "patternProperties", type: "object", schemaType: "object", code(Q) {
    let { gen: X, schema: Y, data: $, parentSchema: W, it: J } = Q, { opts: G } = J, H = (0, yB.allSchemaProperties)(Y), B = H.filter((L) => (0, gB.alwaysValidSchema)(J, Y[L]));
    if (H.length === 0 || B.length === H.length && (!J.opts.unevaluated || J.props === true)) return;
    let z = G.strictSchema && !G.allowMatchingProperties && W.properties, K = X.name("valid");
    if (J.props !== true && !(J.props instanceof a8.Name)) J.props = (0, hB.evaluatedPropsToName)(X, J.props);
    let { props: q } = J;
    U();
    function U() {
      for (let L of H) {
        if (z) V(L);
        if (J.allErrors) F(L);
        else X.var(K, true), F(L), X.if(K);
      }
    }
    function V(L) {
      for (let N in z) if (new RegExp(L).test(N)) (0, gB.checkStrictMode)(J, `property ${N} matches pattern ${L} (use allowMatchingProperties)`);
    }
    function F(L) {
      X.forIn("key", $, (N) => {
        X.if(a8._`${(0, yB.usePattern)(Q, L)}.test(${N})`, () => {
          let w = B.includes(L);
          if (!w) Q.subschema({ keyword: "patternProperties", schemaProp: L, dataProp: N, dataPropType: hB.Type.Str }, K);
          if (J.opts.unevaluated && q !== true) X.assign(a8._`${q}[${N}]`, true);
          else if (!w && !J.allErrors) X.if((0, a8.not)(K), () => X.break());
        });
      });
    }
  } };
  fB.default = oj;
});
var mB = P((lB) => {
  Object.defineProperty(lB, "__esModule", { value: true });
  var aj = e(), sj = { keyword: "not", schemaType: ["object", "boolean"], trackErrors: true, code(Q) {
    let { gen: X, schema: Y, it: $ } = Q;
    if ((0, aj.alwaysValidSchema)($, Y)) {
      Q.fail();
      return;
    }
    let W = X.name("valid");
    Q.subschema({ keyword: "not", compositeRule: true, createErrors: false, allErrors: false }, W), Q.failResult(W, () => Q.reset(), () => Q.error());
  }, error: { message: "must NOT be valid" } };
  lB.default = sj;
});
var pB = P((cB) => {
  Object.defineProperty(cB, "__esModule", { value: true });
  var QR = d0(), XR = { keyword: "anyOf", schemaType: "array", trackErrors: true, code: QR.validateUnion, error: { message: "must match a schema in anyOf" } };
  cB.default = XR;
});
var iB = P((dB) => {
  Object.defineProperty(dB, "__esModule", { value: true });
  var s8 = p(), $R = e(), WR = { message: "must match exactly one schema in oneOf", params: ({ params: Q }) => s8._`{passingSchemas: ${Q.passing}}` }, JR = { keyword: "oneOf", schemaType: "array", trackErrors: true, error: WR, code(Q) {
    let { gen: X, schema: Y, parentSchema: $, it: W } = Q;
    if (!Array.isArray(Y)) throw Error("ajv implementation error");
    if (W.opts.discriminator && $.discriminator) return;
    let J = Y, G = X.let("valid", false), H = X.let("passing", null), B = X.name("_valid");
    Q.setParams({ passing: H }), X.block(z), Q.result(G, () => Q.reset(), () => Q.error(true));
    function z() {
      J.forEach((K, q) => {
        let U;
        if ((0, $R.alwaysValidSchema)(W, K)) X.var(B, true);
        else U = Q.subschema({ keyword: "oneOf", schemaProp: q, compositeRule: true }, B);
        if (q > 0) X.if(s8._`${B} && ${G}`).assign(G, false).assign(H, s8._`[${H}, ${q}]`).else();
        X.if(B, () => {
          if (X.assign(G, true), X.assign(H, q), U) Q.mergeEvaluated(U, s8.Name);
        });
      });
    }
  } };
  dB.default = JR;
});
var rB = P((nB) => {
  Object.defineProperty(nB, "__esModule", { value: true });
  var HR = e(), BR = { keyword: "allOf", schemaType: "array", code(Q) {
    let { gen: X, schema: Y, it: $ } = Q;
    if (!Array.isArray(Y)) throw Error("ajv implementation error");
    let W = X.name("valid");
    Y.forEach((J, G) => {
      if ((0, HR.alwaysValidSchema)($, J)) return;
      let H = Q.subschema({ keyword: "allOf", schemaProp: G }, W);
      Q.ok(W), Q.mergeEvaluated(H);
    });
  } };
  nB.default = BR;
});
var sB = P((aB) => {
  Object.defineProperty(aB, "__esModule", { value: true });
  var e8 = p(), tB = e(), KR = { message: ({ params: Q }) => e8.str`must match "${Q.ifClause}" schema`, params: ({ params: Q }) => e8._`{failingKeyword: ${Q.ifClause}}` }, VR = { keyword: "if", schemaType: ["object", "boolean"], trackErrors: true, error: KR, code(Q) {
    let { gen: X, parentSchema: Y, it: $ } = Q;
    if (Y.then === void 0 && Y.else === void 0) (0, tB.checkStrictMode)($, '"if" without "then" and "else" is ignored');
    let W = oB($, "then"), J = oB($, "else");
    if (!W && !J) return;
    let G = X.let("valid", true), H = X.name("_valid");
    if (B(), Q.reset(), W && J) {
      let K = X.let("ifClause");
      Q.setParams({ ifClause: K }), X.if(H, z("then", K), z("else", K));
    } else if (W) X.if(H, z("then"));
    else X.if((0, e8.not)(H), z("else"));
    Q.pass(G, () => Q.error(true));
    function B() {
      let K = Q.subschema({ keyword: "if", compositeRule: true, createErrors: false, allErrors: false }, H);
      Q.mergeEvaluated(K);
    }
    function z(K, q) {
      return () => {
        let U = Q.subschema({ keyword: K }, H);
        if (X.assign(G, H), Q.mergeValidEvaluated(U, G), q) X.assign(q, e8._`${K}`);
        else Q.setParams({ ifClause: K });
      };
    }
  } };
  function oB(Q, X) {
    let Y = Q.schema[X];
    return Y !== void 0 && !(0, tB.alwaysValidSchema)(Q, Y);
  }
  aB.default = VR;
});
var Qz = P((eB) => {
  Object.defineProperty(eB, "__esModule", { value: true });
  var UR = e(), LR = { keyword: ["then", "else"], schemaType: ["object", "boolean"], code({ keyword: Q, parentSchema: X, it: Y }) {
    if (X.if === void 0) (0, UR.checkStrictMode)(Y, `"${Q}" without "if" is ignored`);
  } };
  eB.default = LR;
});
var Yz = P((Xz) => {
  Object.defineProperty(Xz, "__esModule", { value: true });
  var NR = h$(), DR = NB(), OR = f$(), wR = wB(), MR = AB(), AR = PB(), jR = CB(), RR = l$(), IR = xB(), ER = uB(), bR = mB(), PR = pB(), SR = iB(), ZR = rB(), CR = sB(), kR = Qz();
  function vR(Q = false) {
    let X = [bR.default, PR.default, SR.default, ZR.default, CR.default, kR.default, jR.default, RR.default, AR.default, IR.default, ER.default];
    if (Q) X.push(DR.default, wR.default);
    else X.push(NR.default, OR.default);
    return X.push(MR.default), X;
  }
  Xz.default = vR;
});
var Wz = P(($z) => {
  Object.defineProperty($z, "__esModule", { value: true });
  var U0 = p(), TR = { message: ({ schemaCode: Q }) => U0.str`must match format "${Q}"`, params: ({ schemaCode: Q }) => U0._`{format: ${Q}}` }, xR = { keyword: "format", type: ["number", "string"], schemaType: "string", $data: true, error: TR, code(Q, X) {
    let { gen: Y, data: $, $data: W, schema: J, schemaCode: G, it: H } = Q, { opts: B, errSchemaPath: z, schemaEnv: K, self: q } = H;
    if (!B.validateFormats) return;
    if (W) U();
    else V();
    function U() {
      let F = Y.scopeValue("formats", { ref: q.formats, code: B.code.formats }), L = Y.const("fDef", U0._`${F}[${G}]`), N = Y.let("fType"), w = Y.let("format");
      Y.if(U0._`typeof ${L} == "object" && !(${L} instanceof RegExp)`, () => Y.assign(N, U0._`${L}.type || "string"`).assign(w, U0._`${L}.validate`), () => Y.assign(N, U0._`"string"`).assign(w, L)), Q.fail$data((0, U0.or)(A(), R()));
      function A() {
        if (B.strictSchema === false) return U0.nil;
        return U0._`${G} && !${w}`;
      }
      function R() {
        let S = K.$async ? U0._`(${L}.async ? await ${w}(${$}) : ${w}(${$}))` : U0._`${w}(${$})`, C = U0._`(typeof ${w} == "function" ? ${S} : ${w}.test(${$}))`;
        return U0._`${w} && ${w} !== true && ${N} === ${X} && !${C}`;
      }
    }
    function V() {
      let F = q.formats[J];
      if (!F) {
        A();
        return;
      }
      if (F === true) return;
      let [L, N, w] = R(F);
      if (L === X) Q.pass(S());
      function A() {
        if (B.strictSchema === false) {
          q.logger.warn(C());
          return;
        }
        throw Error(C());
        function C() {
          return `unknown format "${J}" ignored in schema at path "${z}"`;
        }
      }
      function R(C) {
        let K0 = C instanceof RegExp ? (0, U0.regexpCode)(C) : B.code.formats ? U0._`${B.code.formats}${(0, U0.getProperty)(J)}` : void 0, V0 = Y.scopeValue("formats", { key: J, ref: C, code: K0 });
        if (typeof C == "object" && !(C instanceof RegExp)) return [C.type || "string", C.validate, U0._`${V0}.validate`];
        return ["string", C, V0];
      }
      function S() {
        if (typeof F == "object" && !(F instanceof RegExp) && F.async) {
          if (!K.$async) throw Error("async format in sync schema");
          return U0._`await ${w}(${$})`;
        }
        return typeof N == "function" ? U0._`${w}(${$})` : U0._`${w}.test(${$})`;
      }
    }
  } };
  $z.default = xR;
});
var Gz = P((Jz) => {
  Object.defineProperty(Jz, "__esModule", { value: true });
  var gR = Wz(), hR = [gR.default];
  Jz.default = hR;
});
var zz = P((Hz) => {
  Object.defineProperty(Hz, "__esModule", { value: true });
  Hz.contentVocabulary = Hz.metadataVocabulary = void 0;
  Hz.metadataVocabulary = ["title", "description", "default", "deprecated", "readOnly", "writeOnly", "examples"];
  Hz.contentVocabulary = ["contentMediaType", "contentEncoding", "contentSchema"];
});
var qz = P((Vz) => {
  Object.defineProperty(Vz, "__esModule", { value: true });
  var lR = TH(), mR = HB(), cR = Yz(), pR = Gz(), Kz = zz(), dR = [lR.default, mR.default, (0, cR.default)(), pR.default, Kz.metadataVocabulary, Kz.contentVocabulary];
  Vz.default = dR;
});
var Nz = P((Lz) => {
  Object.defineProperty(Lz, "__esModule", { value: true });
  Lz.DiscrError = void 0;
  var Uz;
  (function(Q) {
    Q.Tag = "tag", Q.Mapping = "mapping";
  })(Uz || (Lz.DiscrError = Uz = {}));
});
var wz = P((Oz) => {
  Object.defineProperty(Oz, "__esModule", { value: true });
  var n6 = p(), c$ = Nz(), Dz = x8(), nR = n4(), rR = e(), oR = { message: ({ params: { discrError: Q, tagName: X } }) => Q === c$.DiscrError.Tag ? `tag "${X}" must be string` : `value of tag "${X}" must be in oneOf`, params: ({ params: { discrError: Q, tag: X, tagName: Y } }) => n6._`{error: ${Q}, tag: ${Y}, tagValue: ${X}}` }, tR = { keyword: "discriminator", type: "object", schemaType: "object", error: oR, code(Q) {
    let { gen: X, data: Y, schema: $, parentSchema: W, it: J } = Q, { oneOf: G } = W;
    if (!J.opts.discriminator) throw Error("discriminator: requires discriminator option");
    let H = $.propertyName;
    if (typeof H != "string") throw Error("discriminator: requires propertyName");
    if ($.mapping) throw Error("discriminator: mapping is not supported");
    if (!G) throw Error("discriminator: requires oneOf keyword");
    let B = X.let("valid", false), z = X.const("tag", n6._`${Y}${(0, n6.getProperty)(H)}`);
    X.if(n6._`typeof ${z} == "string"`, () => K(), () => Q.error(false, { discrError: c$.DiscrError.Tag, tag: z, tagName: H })), Q.ok(B);
    function K() {
      let V = U();
      X.if(false);
      for (let F in V) X.elseIf(n6._`${z} === ${F}`), X.assign(B, q(V[F]));
      X.else(), Q.error(false, { discrError: c$.DiscrError.Mapping, tag: z, tagName: H }), X.endIf();
    }
    function q(V) {
      let F = X.name("valid"), L = Q.subschema({ keyword: "oneOf", schemaProp: V }, F);
      return Q.mergeEvaluated(L, n6.Name), F;
    }
    function U() {
      var V;
      let F = {}, L = w(W), N = true;
      for (let S = 0; S < G.length; S++) {
        let C = G[S];
        if ((C === null || C === void 0 ? void 0 : C.$ref) && !(0, rR.schemaHasRulesButRef)(C, J.self.RULES)) {
          let V0 = C.$ref;
          if (C = Dz.resolveRef.call(J.self, J.schemaEnv.root, J.baseId, V0), C instanceof Dz.SchemaEnv) C = C.schema;
          if (C === void 0) throw new nR.default(J.opts.uriResolver, J.baseId, V0);
        }
        let K0 = (V = C === null || C === void 0 ? void 0 : C.properties) === null || V === void 0 ? void 0 : V[H];
        if (typeof K0 != "object") throw Error(`discriminator: oneOf subschemas (or referenced schemas) must have "properties/${H}"`);
        N = N && (L || w(C)), A(K0, S);
      }
      if (!N) throw Error(`discriminator: "${H}" must be required`);
      return F;
      function w({ required: S }) {
        return Array.isArray(S) && S.includes(H);
      }
      function A(S, C) {
        if (S.const) R(S.const, C);
        else if (S.enum) for (let K0 of S.enum) R(K0, C);
        else throw Error(`discriminator: "properties/${H}" must have "const" or "enum"`);
      }
      function R(S, C) {
        if (typeof S != "string" || S in F) throw Error(`discriminator: "${H}" values must be unique strings`);
        F[S] = C;
      }
    }
  } };
  Oz.default = tR;
});
var Mz = P((BT, sR) => {
  sR.exports = { $schema: "http://json-schema.org/draft-07/schema#", $id: "http://json-schema.org/draft-07/schema#", title: "Core schema meta-schema", definitions: { schemaArray: { type: "array", minItems: 1, items: { $ref: "#" } }, nonNegativeInteger: { type: "integer", minimum: 0 }, nonNegativeIntegerDefault0: { allOf: [{ $ref: "#/definitions/nonNegativeInteger" }, { default: 0 }] }, simpleTypes: { enum: ["array", "boolean", "integer", "null", "number", "object", "string"] }, stringArray: { type: "array", items: { type: "string" }, uniqueItems: true, default: [] } }, type: ["object", "boolean"], properties: { $id: { type: "string", format: "uri-reference" }, $schema: { type: "string", format: "uri" }, $ref: { type: "string", format: "uri-reference" }, $comment: { type: "string" }, title: { type: "string" }, description: { type: "string" }, default: true, readOnly: { type: "boolean", default: false }, examples: { type: "array", items: true }, multipleOf: { type: "number", exclusiveMinimum: 0 }, maximum: { type: "number" }, exclusiveMaximum: { type: "number" }, minimum: { type: "number" }, exclusiveMinimum: { type: "number" }, maxLength: { $ref: "#/definitions/nonNegativeInteger" }, minLength: { $ref: "#/definitions/nonNegativeIntegerDefault0" }, pattern: { type: "string", format: "regex" }, additionalItems: { $ref: "#" }, items: { anyOf: [{ $ref: "#" }, { $ref: "#/definitions/schemaArray" }], default: true }, maxItems: { $ref: "#/definitions/nonNegativeInteger" }, minItems: { $ref: "#/definitions/nonNegativeIntegerDefault0" }, uniqueItems: { type: "boolean", default: false }, contains: { $ref: "#" }, maxProperties: { $ref: "#/definitions/nonNegativeInteger" }, minProperties: { $ref: "#/definitions/nonNegativeIntegerDefault0" }, required: { $ref: "#/definitions/stringArray" }, additionalProperties: { $ref: "#" }, definitions: { type: "object", additionalProperties: { $ref: "#" }, default: {} }, properties: { type: "object", additionalProperties: { $ref: "#" }, default: {} }, patternProperties: { type: "object", additionalProperties: { $ref: "#" }, propertyNames: { format: "regex" }, default: {} }, dependencies: { type: "object", additionalProperties: { anyOf: [{ $ref: "#" }, { $ref: "#/definitions/stringArray" }] } }, propertyNames: { $ref: "#" }, const: true, enum: { type: "array", items: true, minItems: 1, uniqueItems: true }, type: { anyOf: [{ $ref: "#/definitions/simpleTypes" }, { type: "array", items: { $ref: "#/definitions/simpleTypes" }, minItems: 1, uniqueItems: true }] }, format: { type: "string" }, contentMediaType: { type: "string" }, contentEncoding: { type: "string" }, if: { $ref: "#" }, then: { $ref: "#" }, else: { $ref: "#" }, allOf: { $ref: "#/definitions/schemaArray" }, anyOf: { $ref: "#/definitions/schemaArray" }, oneOf: { $ref: "#/definitions/schemaArray" }, not: { $ref: "#" } }, default: true };
});
var d$ = P((h0, p$) => {
  Object.defineProperty(h0, "__esModule", { value: true });
  h0.MissingRefError = h0.ValidationError = h0.CodeGen = h0.Name = h0.nil = h0.stringify = h0.str = h0._ = h0.KeywordCxt = h0.Ajv = void 0;
  var eR = IH(), QI = qz(), XI = wz(), Az = Mz(), YI = ["/properties"], QQ = "http://json-schema.org/draft-07/schema";
  class J9 extends eR.default {
    _addVocabularies() {
      if (super._addVocabularies(), QI.default.forEach((Q) => this.addVocabulary(Q)), this.opts.discriminator) this.addKeyword(XI.default);
    }
    _addDefaultMetaSchema() {
      if (super._addDefaultMetaSchema(), !this.opts.meta) return;
      let Q = this.opts.$data ? this.$dataMetaSchema(Az, YI) : Az;
      this.addMetaSchema(Q, QQ, false), this.refs["http://json-schema.org/schema"] = QQ;
    }
    defaultMeta() {
      return this.opts.defaultMeta = super.defaultMeta() || (this.getSchema(QQ) ? QQ : void 0);
    }
  }
  h0.Ajv = J9;
  p$.exports = h0 = J9;
  p$.exports.Ajv = J9;
  Object.defineProperty(h0, "__esModule", { value: true });
  h0.default = J9;
  var $I = i4();
  Object.defineProperty(h0, "KeywordCxt", { enumerable: true, get: function() {
    return $I.KeywordCxt;
  } });
  var r6 = p();
  Object.defineProperty(h0, "_", { enumerable: true, get: function() {
    return r6._;
  } });
  Object.defineProperty(h0, "str", { enumerable: true, get: function() {
    return r6.str;
  } });
  Object.defineProperty(h0, "stringify", { enumerable: true, get: function() {
    return r6.stringify;
  } });
  Object.defineProperty(h0, "nil", { enumerable: true, get: function() {
    return r6.nil;
  } });
  Object.defineProperty(h0, "Name", { enumerable: true, get: function() {
    return r6.Name;
  } });
  Object.defineProperty(h0, "CodeGen", { enumerable: true, get: function() {
    return r6.CodeGen;
  } });
  var WI = _8();
  Object.defineProperty(h0, "ValidationError", { enumerable: true, get: function() {
    return WI.default;
  } });
  var JI = n4();
  Object.defineProperty(h0, "MissingRefError", { enumerable: true, get: function() {
    return JI.default;
  } });
});
var kz = P((Zz) => {
  Object.defineProperty(Zz, "__esModule", { value: true });
  Zz.formatNames = Zz.fastFormats = Zz.fullFormats = void 0;
  function q1(Q, X) {
    return { validate: Q, compare: X };
  }
  Zz.fullFormats = { date: q1(Ez, o$), time: q1(n$(true), t$), "date-time": q1(jz(true), Pz), "iso-time": q1(n$(), bz), "iso-date-time": q1(jz(), Sz), duration: /^P(?!$)((\d+Y)?(\d+M)?(\d+D)?(T(?=\d)(\d+H)?(\d+M)?(\d+S)?)?|(\d+W)?)$/, uri: UI, "uri-reference": /^(?:[a-z][a-z0-9+\-.]*:)?(?:\/?\/(?:(?:[a-z0-9\-._~!$&'()*+,;=:]|%[0-9a-f]{2})*@)?(?:\[(?:(?:(?:(?:[0-9a-f]{1,4}:){6}|::(?:[0-9a-f]{1,4}:){5}|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}|(?:(?:[0-9a-f]{1,4}:){0,1}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::)(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d\d?))|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|[Vv][0-9a-f]+\.[a-z0-9\-._~!$&'()*+,;=:]+)\]|(?:(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d\d?)|(?:[a-z0-9\-._~!$&'"()*+,;=]|%[0-9a-f]{2})*)(?::\d*)?(?:\/(?:[a-z0-9\-._~!$&'"()*+,;=:@]|%[0-9a-f]{2})*)*|\/(?:(?:[a-z0-9\-._~!$&'"()*+,;=:@]|%[0-9a-f]{2})+(?:\/(?:[a-z0-9\-._~!$&'"()*+,;=:@]|%[0-9a-f]{2})*)*)?|(?:[a-z0-9\-._~!$&'"()*+,;=:@]|%[0-9a-f]{2})+(?:\/(?:[a-z0-9\-._~!$&'"()*+,;=:@]|%[0-9a-f]{2})*)*)?(?:\?(?:[a-z0-9\-._~!$&'"()*+,;=:@/?]|%[0-9a-f]{2})*)?(?:#(?:[a-z0-9\-._~!$&'"()*+,;=:@/?]|%[0-9a-f]{2})*)?$/i, "uri-template": /^(?:(?:[^\x00-\x20"'<>%\\^`{|}]|%[0-9a-f]{2})|\{[+#./;?&=,!@|]?(?:[a-z0-9_]|%[0-9a-f]{2})+(?::[1-9][0-9]{0,3}|\*)?(?:,(?:[a-z0-9_]|%[0-9a-f]{2})+(?::[1-9][0-9]{0,3}|\*)?)*\})*$/i, url: /^(?:https?|ftp):\/\/(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z0-9\u{00a1}-\u{ffff}]+-)*[a-z0-9\u{00a1}-\u{ffff}]+)(?:\.(?:[a-z0-9\u{00a1}-\u{ffff}]+-)*[a-z0-9\u{00a1}-\u{ffff}]+)*(?:\.(?:[a-z\u{00a1}-\u{ffff}]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?$/iu, email: /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/i, hostname: /^(?=.{1,253}\.?$)[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\.[a-z0-9](?:[-0-9a-z]{0,61}[0-9a-z])?)*\.?$/i, ipv4: /^(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)\.){3}(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)$/, ipv6: /^((([0-9a-f]{1,4}:){7}([0-9a-f]{1,4}|:))|(([0-9a-f]{1,4}:){6}(:[0-9a-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9a-f]{1,4}:){5}(((:[0-9a-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9a-f]{1,4}:){4}(((:[0-9a-f]{1,4}){1,3})|((:[0-9a-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9a-f]{1,4}:){3}(((:[0-9a-f]{1,4}){1,4})|((:[0-9a-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9a-f]{1,4}:){2}(((:[0-9a-f]{1,4}){1,5})|((:[0-9a-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9a-f]{1,4}:){1}(((:[0-9a-f]{1,4}){1,6})|((:[0-9a-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9a-f]{1,4}){1,7})|((:[0-9a-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))$/i, regex: MI, uuid: /^(?:urn:uuid:)?[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}$/i, "json-pointer": /^(?:\/(?:[^~/]|~0|~1)*)*$/, "json-pointer-uri-fragment": /^#(?:\/(?:[a-z0-9_\-.!$&'()*+,;:=@]|%[0-9a-f]{2}|~0|~1)*)*$/i, "relative-json-pointer": /^(?:0|[1-9][0-9]*)(?:#|(?:\/(?:[^~/]|~0|~1)*)*)$/, byte: LI, int32: { type: "number", validate: DI }, int64: { type: "number", validate: OI }, float: { type: "number", validate: Iz }, double: { type: "number", validate: Iz }, password: true, binary: true };
  Zz.fastFormats = { ...Zz.fullFormats, date: q1(/^\d\d\d\d-[0-1]\d-[0-3]\d$/, o$), time: q1(/^(?:[0-2]\d:[0-5]\d:[0-5]\d|23:59:60)(?:\.\d+)?(?:z|[+-]\d\d(?::?\d\d)?)$/i, t$), "date-time": q1(/^\d\d\d\d-[0-1]\d-[0-3]\dt(?:[0-2]\d:[0-5]\d:[0-5]\d|23:59:60)(?:\.\d+)?(?:z|[+-]\d\d(?::?\d\d)?)$/i, Pz), "iso-time": q1(/^(?:[0-2]\d:[0-5]\d:[0-5]\d|23:59:60)(?:\.\d+)?(?:z|[+-]\d\d(?::?\d\d)?)?$/i, bz), "iso-date-time": q1(/^\d\d\d\d-[0-1]\d-[0-3]\d[t\s](?:[0-2]\d:[0-5]\d:[0-5]\d|23:59:60)(?:\.\d+)?(?:z|[+-]\d\d(?::?\d\d)?)?$/i, Sz), uri: /^(?:[a-z][a-z0-9+\-.]*:)(?:\/?\/)?[^\s]*$/i, "uri-reference": /^(?:(?:[a-z][a-z0-9+\-.]*:)?\/?\/)?(?:[^\\\s#][^\s#]*)?(?:#[^\\\s]*)?$/i, email: /^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)*$/i };
  Zz.formatNames = Object.keys(Zz.fullFormats);
  function BI(Q) {
    return Q % 4 === 0 && (Q % 100 !== 0 || Q % 400 === 0);
  }
  var zI = /^(\d\d\d\d)-(\d\d)-(\d\d)$/, KI = [0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  function Ez(Q) {
    let X = zI.exec(Q);
    if (!X) return false;
    let Y = +X[1], $ = +X[2], W = +X[3];
    return $ >= 1 && $ <= 12 && W >= 1 && W <= ($ === 2 && BI(Y) ? 29 : KI[$]);
  }
  function o$(Q, X) {
    if (!(Q && X)) return;
    if (Q > X) return 1;
    if (Q < X) return -1;
    return 0;
  }
  var i$ = /^(\d\d):(\d\d):(\d\d(?:\.\d+)?)(z|([+-])(\d\d)(?::?(\d\d))?)?$/i;
  function n$(Q) {
    return function(Y) {
      let $ = i$.exec(Y);
      if (!$) return false;
      let W = +$[1], J = +$[2], G = +$[3], H = $[4], B = $[5] === "-" ? -1 : 1, z = +($[6] || 0), K = +($[7] || 0);
      if (z > 23 || K > 59 || Q && !H) return false;
      if (W <= 23 && J <= 59 && G < 60) return true;
      let q = J - K * B, U = W - z * B - (q < 0 ? 1 : 0);
      return (U === 23 || U === -1) && (q === 59 || q === -1) && G < 61;
    };
  }
  function t$(Q, X) {
    if (!(Q && X)) return;
    let Y = (/* @__PURE__ */ new Date("2020-01-01T" + Q)).valueOf(), $ = (/* @__PURE__ */ new Date("2020-01-01T" + X)).valueOf();
    if (!(Y && $)) return;
    return Y - $;
  }
  function bz(Q, X) {
    if (!(Q && X)) return;
    let Y = i$.exec(Q), $ = i$.exec(X);
    if (!(Y && $)) return;
    if (Q = Y[1] + Y[2] + Y[3], X = $[1] + $[2] + $[3], Q > X) return 1;
    if (Q < X) return -1;
    return 0;
  }
  var r$ = /t|\s/i;
  function jz(Q) {
    let X = n$(Q);
    return function($) {
      let W = $.split(r$);
      return W.length === 2 && Ez(W[0]) && X(W[1]);
    };
  }
  function Pz(Q, X) {
    if (!(Q && X)) return;
    let Y = new Date(Q).valueOf(), $ = new Date(X).valueOf();
    if (!(Y && $)) return;
    return Y - $;
  }
  function Sz(Q, X) {
    if (!(Q && X)) return;
    let [Y, $] = Q.split(r$), [W, J] = X.split(r$), G = o$(Y, W);
    if (G === void 0) return;
    return G || t$($, J);
  }
  var VI = /\/|:/, qI = /^(?:[a-z][a-z0-9+\-.]*:)(?:\/?\/(?:(?:[a-z0-9\-._~!$&'()*+,;=:]|%[0-9a-f]{2})*@)?(?:\[(?:(?:(?:(?:[0-9a-f]{1,4}:){6}|::(?:[0-9a-f]{1,4}:){5}|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}|(?:(?:[0-9a-f]{1,4}:){0,1}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::)(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d\d?))|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|[Vv][0-9a-f]+\.[a-z0-9\-._~!$&'()*+,;=:]+)\]|(?:(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d\d?)|(?:[a-z0-9\-._~!$&'()*+,;=]|%[0-9a-f]{2})*)(?::\d*)?(?:\/(?:[a-z0-9\-._~!$&'()*+,;=:@]|%[0-9a-f]{2})*)*|\/(?:(?:[a-z0-9\-._~!$&'()*+,;=:@]|%[0-9a-f]{2})+(?:\/(?:[a-z0-9\-._~!$&'()*+,;=:@]|%[0-9a-f]{2})*)*)?|(?:[a-z0-9\-._~!$&'()*+,;=:@]|%[0-9a-f]{2})+(?:\/(?:[a-z0-9\-._~!$&'()*+,;=:@]|%[0-9a-f]{2})*)*)(?:\?(?:[a-z0-9\-._~!$&'()*+,;=:@/?]|%[0-9a-f]{2})*)?(?:#(?:[a-z0-9\-._~!$&'()*+,;=:@/?]|%[0-9a-f]{2})*)?$/i;
  function UI(Q) {
    return VI.test(Q) && qI.test(Q);
  }
  var Rz = /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/gm;
  function LI(Q) {
    return Rz.lastIndex = 0, Rz.test(Q);
  }
  var FI = -2147483648, NI = 2147483647;
  function DI(Q) {
    return Number.isInteger(Q) && Q <= NI && Q >= FI;
  }
  function OI(Q) {
    return Number.isInteger(Q);
  }
  function Iz() {
    return true;
  }
  var wI = /[^\\]\\Z/;
  function MI(Q) {
    if (wI.test(Q)) return false;
    try {
      return new RegExp(Q), true;
    } catch (X) {
      return false;
    }
  }
});
var _z = P((vz) => {
  Object.defineProperty(vz, "__esModule", { value: true });
  vz.formatLimitDefinition = void 0;
  var jI = d$(), Y1 = p(), n1 = Y1.operators, XQ = { formatMaximum: { okStr: "<=", ok: n1.LTE, fail: n1.GT }, formatMinimum: { okStr: ">=", ok: n1.GTE, fail: n1.LT }, formatExclusiveMaximum: { okStr: "<", ok: n1.LT, fail: n1.GTE }, formatExclusiveMinimum: { okStr: ">", ok: n1.GT, fail: n1.LTE } }, RI = { message: ({ keyword: Q, schemaCode: X }) => Y1.str`should be ${XQ[Q].okStr} ${X}`, params: ({ keyword: Q, schemaCode: X }) => Y1._`{comparison: ${XQ[Q].okStr}, limit: ${X}}` };
  vz.formatLimitDefinition = { keyword: Object.keys(XQ), type: "string", schemaType: "string", $data: true, error: RI, code(Q) {
    let { gen: X, data: Y, schemaCode: $, keyword: W, it: J } = Q, { opts: G, self: H } = J;
    if (!G.validateFormats) return;
    let B = new jI.KeywordCxt(J, H.RULES.all.format.definition, "format");
    if (B.$data) z();
    else K();
    function z() {
      let U = X.scopeValue("formats", { ref: H.formats, code: G.code.formats }), V = X.const("fmt", Y1._`${U}[${B.schemaCode}]`);
      Q.fail$data((0, Y1.or)(Y1._`typeof ${V} != "object"`, Y1._`${V} instanceof RegExp`, Y1._`typeof ${V}.compare != "function"`, q(V)));
    }
    function K() {
      let U = B.schema, V = H.formats[U];
      if (!V || V === true) return;
      if (typeof V != "object" || V instanceof RegExp || typeof V.compare != "function") throw Error(`"${W}": format "${U}" does not define "compare" function`);
      let F = X.scopeValue("formats", { key: U, ref: V, code: G.code.formats ? Y1._`${G.code.formats}${(0, Y1.getProperty)(U)}` : void 0 });
      Q.fail$data(q(F));
    }
    function q(U) {
      return Y1._`${U}.compare(${Y}, ${$}) ${XQ[W].fail} 0`;
    }
  }, dependencies: ["format"] };
  var II = (Q) => {
    return Q.addKeyword(vz.formatLimitDefinition), Q;
  };
  vz.default = II;
});
var gz = P((G9, yz) => {
  Object.defineProperty(G9, "__esModule", { value: true });
  var o6 = kz(), bI = _z(), e$ = p(), Tz = new e$.Name("fullFormats"), PI = new e$.Name("fastFormats"), Q7 = (Q, X = { keywords: true }) => {
    if (Array.isArray(X)) return xz(Q, X, o6.fullFormats, Tz), Q;
    let [Y, $] = X.mode === "fast" ? [o6.fastFormats, PI] : [o6.fullFormats, Tz], W = X.formats || o6.formatNames;
    if (xz(Q, W, Y, $), X.keywords) (0, bI.default)(Q);
    return Q;
  };
  Q7.get = (Q, X = "full") => {
    let $ = (X === "fast" ? o6.fastFormats : o6.fullFormats)[Q];
    if (!$) throw Error(`Unknown format "${Q}"`);
    return $;
  };
  function xz(Q, X, Y, $) {
    var W, J;
    (W = (J = Q.opts.code).formats) !== null && W !== void 0 || (J.formats = e$._`require("ajv-formats/dist/formats").${$}`);
    for (let G of X) Q.addFormat(G, Y[G]);
  }
  yz.exports = G9 = Q7;
  Object.defineProperty(G9, "__esModule", { value: true });
  G9.default = Q7;
});
var UK = 50;
function D6(Q = UK) {
  let X = new AbortController();
  return qK(Q, X.signal), X;
}
var LK = typeof global == "object" && global && global.Object === Object && global;
var L7 = LK;
var FK = typeof self == "object" && self && self.Object === Object && self;
var NK = L7 || FK || Function("return this")();
var O6 = NK;
var DK = O6.Symbol;
var w6 = DK;
var F7 = Object.prototype;
var OK = F7.hasOwnProperty;
var wK = F7.toString;
var e6 = w6 ? w6.toStringTag : void 0;
function MK(Q) {
  var X = OK.call(Q, e6), Y = Q[e6];
  try {
    Q[e6] = void 0;
    var $ = true;
  } catch (J) {
  }
  var W = wK.call(Q);
  if ($) if (X) Q[e6] = Y;
  else delete Q[e6];
  return W;
}
var N7 = MK;
var AK = Object.prototype;
var jK = AK.toString;
function RK(Q) {
  return jK.call(Q);
}
var D7 = RK;
var IK = "[object Null]";
var EK = "[object Undefined]";
var O7 = w6 ? w6.toStringTag : void 0;
function bK(Q) {
  if (Q == null) return Q === void 0 ? EK : IK;
  return O7 && O7 in Object(Q) ? N7(Q) : D7(Q);
}
var w7 = bK;
function PK(Q) {
  var X = typeof Q;
  return Q != null && (X == "object" || X == "function");
}
var K9 = PK;
var SK = "[object AsyncFunction]";
var ZK = "[object Function]";
var CK = "[object GeneratorFunction]";
var kK = "[object Proxy]";
function vK(Q) {
  if (!K9(Q)) return false;
  var X = w7(Q);
  return X == ZK || X == CK || X == SK || X == kK;
}
var M7 = vK;
var _K = O6["__core-js_shared__"];
var V9 = _K;
var A7 = (function() {
  var Q = /[^.]+$/.exec(V9 && V9.keys && V9.keys.IE_PROTO || "");
  return Q ? "Symbol(src)_1." + Q : "";
})();
function TK(Q) {
  return !!A7 && A7 in Q;
}
var j7 = TK;
var xK = Function.prototype;
var yK = xK.toString;
function gK(Q) {
  if (Q != null) {
    try {
      return yK.call(Q);
    } catch (X) {
    }
    try {
      return Q + "";
    } catch (X) {
    }
  }
  return "";
}
var R7 = gK;
var hK = /[\\^$.*+?()[\]{}|]/g;
var fK = /^\[object .+?Constructor\]$/;
var uK = Function.prototype;
var lK = Object.prototype;
var mK = uK.toString;
var cK = lK.hasOwnProperty;
var pK = RegExp("^" + mK.call(cK).replace(hK, "\\$&").replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, "$1.*?") + "$");
function dK(Q) {
  if (!K9(Q) || j7(Q)) return false;
  var X = M7(Q) ? pK : fK;
  return X.test(R7(Q));
}
var I7 = dK;
function iK(Q, X) {
  return Q == null ? void 0 : Q[X];
}
var E7 = iK;
function nK(Q, X) {
  var Y = E7(Q, X);
  return I7(Y) ? Y : void 0;
}
var q9 = nK;
var rK = q9(Object, "create");
var L1 = rK;
function oK() {
  this.__data__ = L1 ? L1(null) : {}, this.size = 0;
}
var b7 = oK;
function tK(Q) {
  var X = this.has(Q) && delete this.__data__[Q];
  return this.size -= X ? 1 : 0, X;
}
var P7 = tK;
var aK = "__lodash_hash_undefined__";
var sK = Object.prototype;
var eK = sK.hasOwnProperty;
function QV(Q) {
  var X = this.__data__;
  if (L1) {
    var Y = X[Q];
    return Y === aK ? void 0 : Y;
  }
  return eK.call(X, Q) ? X[Q] : void 0;
}
var S7 = QV;
var XV = Object.prototype;
var YV = XV.hasOwnProperty;
function $V(Q) {
  var X = this.__data__;
  return L1 ? X[Q] !== void 0 : YV.call(X, Q);
}
var Z7 = $V;
var WV = "__lodash_hash_undefined__";
function JV(Q, X) {
  var Y = this.__data__;
  return this.size += this.has(Q) ? 0 : 1, Y[Q] = L1 && X === void 0 ? WV : X, this;
}
var C7 = JV;
function M6(Q) {
  var X = -1, Y = Q == null ? 0 : Q.length;
  this.clear();
  while (++X < Y) {
    var $ = Q[X];
    this.set($[0], $[1]);
  }
}
M6.prototype.clear = b7;
M6.prototype.delete = P7;
M6.prototype.get = S7;
M6.prototype.has = Z7;
M6.prototype.set = C7;
var HQ = M6;
function GV() {
  this.__data__ = [], this.size = 0;
}
var k7 = GV;
function HV(Q, X) {
  return Q === X || Q !== Q && X !== X;
}
var v7 = HV;
function BV(Q, X) {
  var Y = Q.length;
  while (Y--) if (v7(Q[Y][0], X)) return Y;
  return -1;
}
var Z1 = BV;
var zV = Array.prototype;
var KV = zV.splice;
function VV(Q) {
  var X = this.__data__, Y = Z1(X, Q);
  if (Y < 0) return false;
  var $ = X.length - 1;
  if (Y == $) X.pop();
  else KV.call(X, Y, 1);
  return --this.size, true;
}
var _7 = VV;
function qV(Q) {
  var X = this.__data__, Y = Z1(X, Q);
  return Y < 0 ? void 0 : X[Y][1];
}
var T7 = qV;
function UV(Q) {
  return Z1(this.__data__, Q) > -1;
}
var x7 = UV;
function LV(Q, X) {
  var Y = this.__data__, $ = Z1(Y, Q);
  if ($ < 0) ++this.size, Y.push([Q, X]);
  else Y[$][1] = X;
  return this;
}
var y7 = LV;
function A6(Q) {
  var X = -1, Y = Q == null ? 0 : Q.length;
  this.clear();
  while (++X < Y) {
    var $ = Q[X];
    this.set($[0], $[1]);
  }
}
A6.prototype.clear = k7;
A6.prototype.delete = _7;
A6.prototype.get = T7;
A6.prototype.has = x7;
A6.prototype.set = y7;
var g7 = A6;
var FV = q9(O6, "Map");
var h7 = FV;
function NV() {
  this.size = 0, this.__data__ = { hash: new HQ(), map: new (h7 || g7)(), string: new HQ() };
}
var f7 = NV;
function DV(Q) {
  var X = typeof Q;
  return X == "string" || X == "number" || X == "symbol" || X == "boolean" ? Q !== "__proto__" : Q === null;
}
var u7 = DV;
function OV(Q, X) {
  var Y = Q.__data__;
  return u7(X) ? Y[typeof X == "string" ? "string" : "hash"] : Y.map;
}
var C1 = OV;
function wV(Q) {
  var X = C1(this, Q).delete(Q);
  return this.size -= X ? 1 : 0, X;
}
var l7 = wV;
function MV(Q) {
  return C1(this, Q).get(Q);
}
var m7 = MV;
function AV(Q) {
  return C1(this, Q).has(Q);
}
var c7 = AV;
function jV(Q, X) {
  var Y = C1(this, Q), $ = Y.size;
  return Y.set(Q, X), this.size += Y.size == $ ? 0 : 1, this;
}
var p7 = jV;
function j6(Q) {
  var X = -1, Y = Q == null ? 0 : Q.length;
  this.clear();
  while (++X < Y) {
    var $ = Q[X];
    this.set($[0], $[1]);
  }
}
j6.prototype.clear = f7;
j6.prototype.delete = l7;
j6.prototype.get = m7;
j6.prototype.has = c7;
j6.prototype.set = p7;
var BQ = j6;
var RV = "Expected a function";
function zQ(Q, X) {
  if (typeof Q != "function" || X != null && typeof X != "function") throw TypeError(RV);
  var Y = function() {
    var $ = arguments, W = X ? X.apply(this, $) : $[0], J = Y.cache;
    if (J.has(W)) return J.get(W);
    var G = Q.apply(this, $);
    return Y.cache = J.set(W, G) || J, G;
  };
  return Y.cache = new (zQ.Cache || BQ)(), Y;
}
zQ.Cache = BQ;
var k1 = zQ;
function IV(Q, X) {
  if (Q.destroyed) return;
  Q.write(X);
}
function d7(Q) {
  IV(process.stderr, Q);
}
var i7 = k1((Q) => {
  if (!Q || Q.trim() === "") return null;
  let X = Q.split(",").map((J) => J.trim()).filter(Boolean);
  if (X.length === 0) return null;
  let Y = X.some((J) => J.startsWith("!")), $ = X.some((J) => !J.startsWith("!"));
  if (Y && $) return null;
  let W = X.map((J) => J.replace(/^!/, "").toLowerCase());
  return { include: Y ? [] : W, exclude: Y ? W : [], isExclusive: Y };
});
function EV(Q) {
  let X = [], Y = Q.match(/^MCP server ["']([^"']+)["']/);
  if (Y && Y[1]) X.push("mcp"), X.push(Y[1].toLowerCase());
  else {
    let J = Q.match(/^([^:[]+):/);
    if (J && J[1]) X.push(J[1].trim().toLowerCase());
  }
  let $ = Q.match(/^\[([^\]]+)]/);
  if ($ && $[1]) X.push($[1].trim().toLowerCase());
  if (Q.toLowerCase().includes("1p event:")) X.push("1p");
  let W = Q.match(/:\s*([^:]+?)(?:\s+(?:type|mode|status|event))?:/);
  if (W && W[1]) {
    let J = W[1].trim().toLowerCase();
    if (J.length < 30 && !J.includes(" ")) X.push(J);
  }
  return Array.from(new Set(X));
}
function bV(Q, X) {
  if (!X) return true;
  if (Q.length === 0) return false;
  if (X.isExclusive) return !Q.some((Y) => X.exclude.includes(Y));
  else return Q.some((Y) => X.include.includes(Y));
}
function n7(Q, X) {
  if (!X) return true;
  let Y = EV(Q);
  return bV(Y, X);
}
function U9() {
  return (process.env.CLAUDE_CONFIG_DIR ?? PV(SV(), ".claude")).normalize("NFC");
}
function KQ(Q) {
  if (!Q) return false;
  if (typeof Q === "boolean") return Q;
  let X = Q.toLowerCase().trim();
  return ["1", "true", "yes", "on"].includes(X);
}
function kV() {
  let Q = "";
  if (typeof process < "u" && typeof process.cwd === "function" && typeof r7 === "function") Q = r7(ZV()).normalize("NFC");
  return { originalCwd: Q, projectRoot: Q, totalCostUSD: 0, totalAPIDuration: 0, totalAPIDurationWithoutRetries: 0, totalToolDuration: 0, startTime: Date.now(), lastInteractionTime: Date.now(), totalLinesAdded: 0, totalLinesRemoved: 0, hasUnknownModelCost: false, cwd: Q, modelUsage: {}, mainLoopModelOverride: void 0, initialMainLoopModel: null, modelStrings: null, isInteractive: false, clientType: "cli", sessionIngressToken: void 0, oauthTokenFromFd: void 0, apiKeyFromFd: void 0, flagSettingsPath: void 0, allowedSettingSources: ["userSettings", "projectSettings", "localSettings", "flagSettings", "policySettings"], meter: null, sessionCounter: null, locCounter: null, prCounter: null, commitCounter: null, costCounter: null, tokenCounter: null, codeEditToolDecisionCounter: null, activeTimeCounter: null, sessionId: CV(), parentSessionId: void 0, loggerProvider: null, eventLogger: null, meterProvider: null, tracerProvider: null, agentColorMap: /* @__PURE__ */ new Map(), agentColorIndex: 0, lastAPIRequest: null, inMemoryErrorLog: [], inlinePlugins: [], chromeFlagOverride: void 0, useCoworkPlugins: false, sessionBypassPermissionsMode: false, sessionTrustAccepted: false, sessionPersistenceDisabled: false, hasExitedPlanMode: false, needsPlanModeExitAttachment: false, hasExitedDelegateMode: false, needsDelegateModeExitAttachment: false, lspRecommendationShownThisSession: false, initJsonSchema: null, registeredHooks: null, planSlugCache: /* @__PURE__ */ new Map(), teleportedSessionInfo: null, invokedSkills: /* @__PURE__ */ new Map(), slowOperations: [], promptCacheBreaks: [], sdkBetas: void 0, mainThreadAgentType: void 0, isRemoteMode: false, directConnectServerUrl: void 0, systemPromptSectionCache: /* @__PURE__ */ new Map(), lastEmittedDate: null, additionalDirectoriesForClaudeMd: [], resumedTranscriptPath: null, promptCache1hAllowlist: null };
}
var vV = kV();
function o7() {
  return vV.sessionId;
}
function t7({ writeFn: Q, flushIntervalMs: X = 1e3, maxBufferSize: Y = 100, immediateMode: $ = false }) {
  let W = [], J = null;
  function G() {
    if (J) clearTimeout(J), J = null;
  }
  function H() {
    if (W.length === 0) return;
    Q(W.join("")), W = [], G();
  }
  function B() {
    if (!J) J = setTimeout(H, X);
  }
  return { write(z) {
    if ($) {
      Q(z);
      return;
    }
    if (W.push(z), B(), W.length >= Y) H();
  }, flush: H, dispose() {
    H();
  } };
}
var a7 = /* @__PURE__ */ new Set();
function s7(Q) {
  return a7.add(Q), () => a7.delete(Q);
}
var VQ = (() => {
  let Q = process.env.CLAUDE_CODE_SLOW_OPERATION_THRESHOLD_MS;
  if (Q !== void 0) {
    let X = Number(Q);
    if (!Number.isNaN(X) && X >= 0) return X;
  }
  return 1 / 0;
})();
function _V(Q) {
  if (Q === null) return "null";
  if (Q === void 0) return "undefined";
  if (Array.isArray(Q)) return `Array[${Q.length}]`;
  if (typeof Q === "object") return `Object{${Object.keys(Q).length} keys}`;
  if (typeof Q === "string") return `string(${Q.length} chars)`;
  return typeof Q;
}
function e7(Q, X) {
  let Y = performance.now();
  try {
    return X();
  } finally {
    performance.now() - Y > VQ;
  }
}
function Z0(Q, X, Y) {
  let $ = _V(Q);
  return e7(`JSON.stringify(${$})`, () => JSON.stringify(Q, X, Y));
}
var L9 = (Q, X) => {
  let Y = typeof Q === "string" ? Q.length : 0;
  return e7(`JSON.parse(${Y} chars)`, () => JSON.parse(Q, X));
};
var TV = k1(() => {
  return KQ(process.env.DEBUG) || KQ(process.env.DEBUG_SDK) || process.argv.includes("--debug") || process.argv.includes("-d") || YW() || process.argv.some((Q) => Q.startsWith("--debug=")) || $W() !== null;
});
var xV = k1(() => {
  let Q = process.argv.find((Y) => Y.startsWith("--debug="));
  if (!Q) return null;
  let X = Q.substring(8);
  return i7(X);
});
var YW = k1(() => {
  return process.argv.includes("--debug-to-stderr") || process.argv.includes("-d2e");
});
var $W = k1(() => {
  for (let Q = 0; Q < process.argv.length; Q++) {
    let X = process.argv[Q];
    if (X.startsWith("--debug-file=")) return X.substring(13);
    if (X === "--debug-file" && Q + 1 < process.argv.length) return process.argv[Q + 1];
  }
  return null;
});
function yV(Q) {
  if (typeof process > "u" || typeof process.versions > "u" || typeof process.versions.node > "u") return false;
  let X = xV();
  return n7(Q, X);
}
var gV = false;
var F9 = null;
function hV() {
  if (!F9) {
    let Q = null;
    F9 = t7({ writeFn: (X) => {
      let Y = WW(), $ = QW(Y);
      if (Q !== $) {
        try {
          $1().mkdirSync($);
        } catch {
        }
        Q = $;
      }
      $1().appendFileSync(Y, X), fV();
    }, flushIntervalMs: 1e3, maxBufferSize: 100, immediateMode: TV() }), s7(async () => F9?.dispose());
  }
  return F9;
}
function v1(Q, { level: X } = { level: "debug" }) {
  if (!yV(Q)) return;
  if (gV && Q.includes(`
`)) Q = Z0(Q);
  let $ = `${(/* @__PURE__ */ new Date()).toISOString()} [${X.toUpperCase()}] ${Q.trim()}
`;
  if (YW()) {
    d7($);
    return;
  }
  hV().write($);
}
function WW() {
  return $W() ?? process.env.CLAUDE_CODE_DEBUG_LOGS_DIR ?? XW(U9(), "debug", `${o7()}.txt`);
}
var fV = k1(() => {
  if (process.argv[2] === "--ripgrep") return;
  try {
    let Q = WW(), X = QW(Q), Y = XW(X, "latest");
    if (!$1().existsSync(X)) $1().mkdirSync(X);
    if ($1().existsSync(Y)) try {
      $1().unlinkSync(Y);
    } catch {
    }
    $1().symlinkSync(Q, Y);
  } catch {
  }
});
function F0(Q, X) {
  let Y = performance.now();
  try {
    return X();
  } finally {
    performance.now() - Y > VQ;
  }
}
var oV = { cwd() {
  return process.cwd();
}, existsSync(Q) {
  return F0(`existsSync(${Q})`, () => f.existsSync(Q));
}, async stat(Q) {
  return uV(Q);
}, async readdir(Q) {
  return lV(Q, { withFileTypes: true });
}, async unlink(Q) {
  return mV(Q);
}, async rmdir(Q) {
  return cV(Q);
}, async rm(Q, X) {
  return pV(Q, X);
}, async mkdir(Q, X) {
  await dV(Q, { recursive: true, ...X });
}, async readFile(Q, X) {
  return JW(Q, { encoding: X.encoding });
}, async rename(Q, X) {
  return iV(Q, X);
}, statSync(Q) {
  return F0(`statSync(${Q})`, () => f.statSync(Q));
}, lstatSync(Q) {
  return F0(`lstatSync(${Q})`, () => f.lstatSync(Q));
}, readFileSync(Q, X) {
  return F0(`readFileSync(${Q})`, () => f.readFileSync(Q, { encoding: X.encoding }));
}, readFileBytesSync(Q) {
  return F0(`readFileBytesSync(${Q})`, () => f.readFileSync(Q));
}, readSync(Q, X) {
  return F0(`readSync(${Q}, ${X.length} bytes)`, () => {
    let Y = void 0;
    try {
      Y = f.openSync(Q, "r");
      let $ = Buffer.alloc(X.length), W = f.readSync(Y, $, 0, X.length, 0);
      return { buffer: $, bytesRead: W };
    } finally {
      if (Y) f.closeSync(Y);
    }
  });
}, appendFileSync(Q, X, Y) {
  return F0(`appendFileSync(${Q}, ${X.length} chars)`, () => {
    if (!f.existsSync(Q) && Y?.mode !== void 0) {
      let $ = f.openSync(Q, "a", Y.mode);
      try {
        f.appendFileSync($, X);
      } finally {
        f.closeSync($);
      }
    } else f.appendFileSync(Q, X);
  });
}, copyFileSync(Q, X) {
  return F0(`copyFileSync(${Q} \u2192 ${X})`, () => f.copyFileSync(Q, X));
}, unlinkSync(Q) {
  return F0(`unlinkSync(${Q})`, () => f.unlinkSync(Q));
}, renameSync(Q, X) {
  return F0(`renameSync(${Q} \u2192 ${X})`, () => f.renameSync(Q, X));
}, linkSync(Q, X) {
  return F0(`linkSync(${Q} \u2192 ${X})`, () => f.linkSync(Q, X));
}, symlinkSync(Q, X, Y) {
  return F0(`symlinkSync(${Q} \u2192 ${X})`, () => f.symlinkSync(Q, X, Y));
}, readlinkSync(Q) {
  return F0(`readlinkSync(${Q})`, () => f.readlinkSync(Q));
}, realpathSync(Q) {
  return F0(`realpathSync(${Q})`, () => f.realpathSync(Q).normalize("NFC"));
}, mkdirSync(Q, X) {
  return F0(`mkdirSync(${Q})`, () => {
    if (!f.existsSync(Q)) {
      let Y = { recursive: true };
      if (X?.mode !== void 0) Y.mode = X.mode;
      f.mkdirSync(Q, Y);
    }
  });
}, readdirSync(Q) {
  return F0(`readdirSync(${Q})`, () => f.readdirSync(Q, { withFileTypes: true }));
}, readdirStringSync(Q) {
  return F0(`readdirStringSync(${Q})`, () => f.readdirSync(Q));
}, isDirEmptySync(Q) {
  return F0(`isDirEmptySync(${Q})`, () => {
    return this.readdirSync(Q).length === 0;
  });
}, rmdirSync(Q) {
  return F0(`rmdirSync(${Q})`, () => f.rmdirSync(Q));
}, rmSync(Q, X) {
  return F0(`rmSync(${Q})`, () => f.rmSync(Q, X));
}, createWriteStream(Q) {
  return f.createWriteStream(Q);
}, async readFileBytes(Q, X) {
  if (X === void 0) return JW(Q);
  let Y = await nV(Q, "r");
  try {
    let { size: $ } = await Y.stat(), W = Math.min($, X), J = Buffer.allocUnsafe(W), G = 0;
    while (G < W) {
      let { bytesRead: H } = await Y.read(J, G, W - G, G);
      if (H === 0) break;
      G += H;
    }
    return G < W ? J.subarray(0, G) : J;
  } finally {
    await Y.close();
  }
} };
var tV = oV;
function $1() {
  return tV;
}
var F1 = class extends Error {
};
function R6() {
  return process.versions.bun !== void 0;
}
var N9 = null;
var HW = false;
function $q() {
  if (HW) return N9;
  if (HW = true, !process.env.DEBUG_CLAUDE_AGENT_SDK) return null;
  let Q = GW(U9(), "debug");
  if (N9 = GW(Q, `sdk-${eV()}.txt`), !Xq(Q)) Yq(Q, { recursive: true });
  return process.stderr.write(`SDK debug logs: ${N9}
`), N9;
}
function N1(Q) {
  let X = $q();
  if (!X) return;
  let $ = `${(/* @__PURE__ */ new Date()).toISOString()} ${Q}
`;
  Qq(X, $);
}
function BW(Q, X) {
  let Y = { ...Q };
  if (X) {
    let $ = { sandbox: X };
    if (Y.settings) try {
      $ = { ...L9(Y.settings), sandbox: X };
    } catch {
    }
    Y.settings = Z0($);
  }
  return Y;
}
var Q4 = class {
  options;
  process;
  processStdin;
  processStdout;
  ready = false;
  abortController;
  exitError;
  exitListeners = [];
  processExitHandler;
  abortHandler;
  constructor(Q) {
    this.options = Q;
    this.abortController = Q.abortController || D6(), this.initialize();
  }
  getDefaultExecutable() {
    return R6() ? "bun" : "node";
  }
  spawnLocalProcess(Q) {
    let { command: X, args: Y, cwd: $, env: W, signal: J } = Q, G = W.DEBUG_CLAUDE_AGENT_SDK || this.options.stderr ? "pipe" : "ignore", H = Wq(X, Y, { cwd: $, stdio: ["pipe", "pipe", G], signal: J, env: W, windowsHide: true });
    if (W.DEBUG_CLAUDE_AGENT_SDK || this.options.stderr) H.stderr.on("data", (z) => {
      let K = z.toString();
      if (N1(K), this.options.stderr) this.options.stderr(K);
    });
    return { stdin: H.stdin, stdout: H.stdout, get killed() {
      return H.killed;
    }, get exitCode() {
      return H.exitCode;
    }, kill: H.kill.bind(H), on: H.on.bind(H), once: H.once.bind(H), off: H.off.bind(H) };
  }
  initialize() {
    try {
      let { additionalDirectories: Q = [], agent: X, betas: Y, cwd: $, executable: W = this.getDefaultExecutable(), executableArgs: J = [], extraArgs: G = {}, pathToClaudeCodeExecutable: H, env: B = { ...process.env }, maxThinkingTokens: z, maxTurns: K, maxBudgetUsd: q, model: U, fallbackModel: V, jsonSchema: F, permissionMode: L, allowDangerouslySkipPermissions: N, permissionPromptToolName: w, continueConversation: A, resume: R, settingSources: S, allowedTools: C = [], disallowedTools: K0 = [], tools: V0, mcpServers: s, strictMcpConfig: O0, canUseTool: L0, includePartialMessages: U1, plugins: P1, sandbox: o1 } = this.options, m = ["--output-format", "stream-json", "--verbose", "--input-format", "stream-json"];
      if (z !== void 0) m.push("--max-thinking-tokens", z.toString());
      if (this.options.effort) m.push("--effort", this.options.effort);
      if (K) m.push("--max-turns", K.toString());
      if (q !== void 0) m.push("--max-budget-usd", q.toString());
      if (U) m.push("--model", U);
      if (X) m.push("--agent", X);
      if (Y && Y.length > 0) m.push("--betas", Y.join(","));
      if (F) m.push("--json-schema", Z0(F));
      if (this.options.debugFile) m.push("--debug-file", this.options.debugFile);
      else if (this.options.debug) m.push("--debug");
      if (B.DEBUG_CLAUDE_AGENT_SDK) m.push("--debug-to-stderr");
      if (L0) {
        if (w) throw Error("canUseTool callback cannot be used with permissionPromptToolName. Please use one or the other.");
        m.push("--permission-prompt-tool", "stdio");
      } else if (w) m.push("--permission-prompt-tool", w);
      if (A) m.push("--continue");
      if (R) m.push("--resume", R);
      if (C.length > 0) m.push("--allowedTools", C.join(","));
      if (K0.length > 0) m.push("--disallowedTools", K0.join(","));
      if (V0 !== void 0) if (Array.isArray(V0)) if (V0.length === 0) m.push("--tools", "");
      else m.push("--tools", V0.join(","));
      else m.push("--tools", "default");
      if (s && Object.keys(s).length > 0) m.push("--mcp-config", Z0({ mcpServers: s }));
      if (S) m.push("--setting-sources", S.join(","));
      if (O0) m.push("--strict-mcp-config");
      if (L) m.push("--permission-mode", L);
      if (N) m.push("--allow-dangerously-skip-permissions");
      if (V) {
        if (U && V === U) throw Error("Fallback model cannot be the same as the main model. Please specify a different model for fallbackModel option.");
        m.push("--fallback-model", V);
      }
      if (U1) m.push("--include-partial-messages");
      for (let E0 of Q) m.push("--add-dir", E0);
      if (P1 && P1.length > 0) for (let E0 of P1) if (E0.type === "local") m.push("--plugin-dir", E0.path);
      else throw Error(`Unsupported plugin type: ${E0.type}`);
      if (this.options.forkSession) m.push("--fork-session");
      if (this.options.resumeSessionAt) m.push("--resume-session-at", this.options.resumeSessionAt);
      if (this.options.sessionId) m.push("--session-id", this.options.sessionId);
      if (this.options.persistSession === false) m.push("--no-session-persistence");
      let YQ = BW(G ?? {}, o1);
      for (let [E0, S1] of Object.entries(YQ)) if (S1 === null) m.push(`--${E0}`);
      else m.push(`--${E0}`, S1);
      if (!B.CLAUDE_CODE_ENTRYPOINT) B.CLAUDE_CODE_ENTRYPOINT = "sdk-ts";
      if (delete B.NODE_OPTIONS, B.DEBUG_CLAUDE_AGENT_SDK) B.DEBUG = "1";
      else delete B.DEBUG;
      let t1 = Gq(H), t6 = t1 ? H : W, a6 = t1 ? [...J, ...m] : [...J, H, ...m], B9 = { command: t6, args: a6, cwd: $, env: B, signal: this.abortController.signal };
      if (this.options.spawnClaudeCodeProcess) N1(`Spawning Claude Code (custom): ${t6} ${a6.join(" ")}`), this.process = this.options.spawnClaudeCodeProcess(B9);
      else {
        if (!$1().existsSync(H)) {
          let S1 = t1 ? `Claude Code native binary not found at ${H}. Please ensure Claude Code is installed via native installer or specify a valid path with options.pathToClaudeCodeExecutable.` : `Claude Code executable not found at ${H}. Is options.pathToClaudeCodeExecutable set?`;
          throw ReferenceError(S1);
        }
        N1(`Spawning Claude Code: ${t6} ${a6.join(" ")}`), this.process = this.spawnLocalProcess(B9);
      }
      this.processStdin = this.process.stdin, this.processStdout = this.process.stdout;
      let z9 = () => {
        if (this.process && !this.process.killed) this.process.kill("SIGTERM");
      };
      this.processExitHandler = z9, this.abortHandler = z9, process.on("exit", this.processExitHandler), this.abortController.signal.addEventListener("abort", this.abortHandler), this.process.on("error", (E0) => {
        if (this.ready = false, this.abortController.signal.aborted) this.exitError = new F1("Claude Code process aborted by user");
        else this.exitError = Error(`Failed to spawn Claude Code process: ${E0.message}`), N1(this.exitError.message);
      }), this.process.on("exit", (E0, S1) => {
        if (this.ready = false, this.abortController.signal.aborted) this.exitError = new F1("Claude Code process aborted by user");
        else {
          let U6 = this.getProcessExitError(E0, S1);
          if (U6) this.exitError = U6, N1(U6.message);
        }
      }), this.ready = true;
    } catch (Q) {
      throw this.ready = false, Q;
    }
  }
  getProcessExitError(Q, X) {
    if (Q !== 0 && Q !== null) return Error(`Claude Code process exited with code ${Q}`);
    else if (X) return Error(`Claude Code process terminated by signal ${X}`);
    return;
  }
  write(Q) {
    if (this.abortController.signal.aborted) throw new F1("Operation aborted");
    if (!this.ready || !this.processStdin) throw Error("ProcessTransport is not ready for writing");
    if (this.process?.killed || this.process?.exitCode !== null) throw Error("Cannot write to terminated process");
    if (this.exitError) throw Error(`Cannot write to process that exited with error: ${this.exitError.message}`);
    N1(`[ProcessTransport] Writing to stdin: ${Q.substring(0, 100)}`);
    try {
      if (!this.processStdin.write(Q)) N1("[ProcessTransport] Write buffer full, data queued");
    } catch (X) {
      throw this.ready = false, Error(`Failed to write to process stdin: ${X.message}`);
    }
  }
  close() {
    if (this.processStdin) this.processStdin.end(), this.processStdin = void 0;
    if (this.abortHandler) this.abortController.signal.removeEventListener("abort", this.abortHandler), this.abortHandler = void 0;
    for (let { handler: Q } of this.exitListeners) this.process?.off("exit", Q);
    if (this.exitListeners = [], this.process && !this.process.killed) this.process.kill("SIGTERM"), setTimeout(() => {
      if (this.process && !this.process.killed) this.process.kill("SIGKILL");
    }, 5e3);
    if (this.ready = false, this.processExitHandler) process.off("exit", this.processExitHandler), this.processExitHandler = void 0;
  }
  isReady() {
    return this.ready;
  }
  async *readMessages() {
    if (!this.processStdout) throw Error("ProcessTransport output stream not available");
    let Q = Jq({ input: this.processStdout });
    try {
      for await (let X of Q) if (X.trim()) try {
        yield L9(X);
      } catch (Y) {
        throw N1(`Non-JSON stdout: ${X}`), Error(`CLI output was not valid JSON. This may indicate an error during startup. Output: ${X.slice(0, 200)}${X.length > 200 ? "..." : ""}`);
      }
      await this.waitForExit();
    } catch (X) {
      throw X;
    } finally {
      Q.close();
    }
  }
  endInput() {
    if (this.processStdin) this.processStdin.end();
  }
  getInputStream() {
    return this.processStdin;
  }
  onExit(Q) {
    if (!this.process) return () => {
    };
    let X = (Y, $) => {
      let W = this.getProcessExitError(Y, $);
      Q(W);
    };
    return this.process.on("exit", X), this.exitListeners.push({ callback: Q, handler: X }), () => {
      if (this.process) this.process.off("exit", X);
      let Y = this.exitListeners.findIndex(($) => $.handler === X);
      if (Y !== -1) this.exitListeners.splice(Y, 1);
    };
  }
  async waitForExit() {
    if (!this.process) {
      if (this.exitError) throw this.exitError;
      return;
    }
    if (this.process.exitCode !== null || this.process.killed) {
      if (this.exitError) throw this.exitError;
      return;
    }
    return new Promise((Q, X) => {
      let Y = (W, J) => {
        if (this.abortController.signal.aborted) {
          X(new F1("Operation aborted"));
          return;
        }
        let G = this.getProcessExitError(W, J);
        if (G) X(G);
        else Q();
      };
      this.process.once("exit", Y);
      let $ = (W) => {
        this.process.off("exit", Y), X(W);
      };
      this.process.once("error", $), this.process.once("exit", () => {
        this.process.off("error", $);
      });
    });
  }
};
function Gq(Q) {
  return ![".js", ".mjs", ".tsx", ".ts", ".jsx"].some((Y) => Q.endsWith(Y));
}
var X4 = class {
  returned;
  queue = [];
  readResolve;
  readReject;
  isDone = false;
  hasError;
  started = false;
  constructor(Q) {
    this.returned = Q;
  }
  [Symbol.asyncIterator]() {
    if (this.started) throw Error("Stream can only be iterated once");
    return this.started = true, this;
  }
  next() {
    if (this.queue.length > 0) return Promise.resolve({ done: false, value: this.queue.shift() });
    if (this.isDone) return Promise.resolve({ done: true, value: void 0 });
    if (this.hasError) return Promise.reject(this.hasError);
    return new Promise((Q, X) => {
      this.readResolve = Q, this.readReject = X;
    });
  }
  enqueue(Q) {
    if (this.readResolve) {
      let X = this.readResolve;
      this.readResolve = void 0, this.readReject = void 0, X({ done: false, value: Q });
    } else this.queue.push(Q);
  }
  done() {
    if (this.isDone = true, this.readResolve) {
      let Q = this.readResolve;
      this.readResolve = void 0, this.readReject = void 0, Q({ done: true, value: void 0 });
    }
  }
  error(Q) {
    if (this.hasError = Q, this.readReject) {
      let X = this.readReject;
      this.readResolve = void 0, this.readReject = void 0, X(Q);
    }
  }
  return() {
    if (this.isDone = true, this.returned) this.returned();
    return Promise.resolve({ done: true, value: void 0 });
  }
};
var qQ = class {
  sendMcpMessage;
  isClosed = false;
  constructor(Q) {
    this.sendMcpMessage = Q;
  }
  onclose;
  onerror;
  onmessage;
  async start() {
  }
  async send(Q) {
    if (this.isClosed) throw Error("Transport is closed");
    this.sendMcpMessage(Q);
  }
  async close() {
    if (this.isClosed) return;
    this.isClosed = true, this.onclose?.();
  }
};
var Y4 = class {
  transport;
  isSingleUserTurn;
  canUseTool;
  hooks;
  abortController;
  jsonSchema;
  initConfig;
  pendingControlResponses = /* @__PURE__ */ new Map();
  cleanupPerformed = false;
  sdkMessages;
  inputStream = new X4();
  initialization;
  cancelControllers = /* @__PURE__ */ new Map();
  hookCallbacks = /* @__PURE__ */ new Map();
  nextCallbackId = 0;
  sdkMcpTransports = /* @__PURE__ */ new Map();
  sdkMcpServerInstances = /* @__PURE__ */ new Map();
  pendingMcpResponses = /* @__PURE__ */ new Map();
  firstResultReceivedResolve;
  firstResultReceived = false;
  hasBidirectionalNeeds() {
    return this.sdkMcpTransports.size > 0 || this.hooks !== void 0 && Object.keys(this.hooks).length > 0 || this.canUseTool !== void 0;
  }
  constructor(Q, X, Y, $, W, J = /* @__PURE__ */ new Map(), G, H) {
    this.transport = Q;
    this.isSingleUserTurn = X;
    this.canUseTool = Y;
    this.hooks = $;
    this.abortController = W;
    this.jsonSchema = G;
    this.initConfig = H;
    for (let [B, z] of J) this.connectSdkMcpServer(B, z);
    this.sdkMessages = this.readSdkMessages(), this.readMessages(), this.initialization = this.initialize(), this.initialization.catch(() => {
    });
  }
  setError(Q) {
    this.inputStream.error(Q);
  }
  async stopTask(Q) {
    await this.request({ subtype: "stop_task", task_id: Q });
  }
  close() {
    this.cleanup();
  }
  cleanup(Q) {
    if (this.cleanupPerformed) return;
    this.cleanupPerformed = true;
    try {
      this.transport.close();
      let X = Error("Query closed before response received");
      for (let { reject: Y } of this.pendingControlResponses.values()) Y(X);
      this.pendingControlResponses.clear();
      for (let { reject: Y } of this.pendingMcpResponses.values()) Y(X);
      this.pendingMcpResponses.clear(), this.cancelControllers.clear(), this.hookCallbacks.clear();
      for (let Y of this.sdkMcpTransports.values()) try {
        Y.close();
      } catch {
      }
      if (this.sdkMcpTransports.clear(), Q) this.inputStream.error(Q);
      else this.inputStream.done();
    } catch (X) {
    }
  }
  next(...[Q]) {
    return this.sdkMessages.next(...[Q]);
  }
  return(Q) {
    return this.sdkMessages.return(Q);
  }
  throw(Q) {
    return this.sdkMessages.throw(Q);
  }
  [Symbol.asyncIterator]() {
    return this.sdkMessages;
  }
  [Symbol.asyncDispose]() {
    return this.sdkMessages[Symbol.asyncDispose]();
  }
  async readMessages() {
    try {
      for await (let Q of this.transport.readMessages()) {
        if (Q.type === "control_response") {
          let X = this.pendingControlResponses.get(Q.response.request_id);
          if (X) X.handler(Q.response);
          continue;
        } else if (Q.type === "control_request") {
          this.handleControlRequest(Q);
          continue;
        } else if (Q.type === "control_cancel_request") {
          this.handleControlCancelRequest(Q);
          continue;
        } else if (Q.type === "keep_alive") continue;
        if (Q.type === "streamlined_text" || Q.type === "streamlined_tool_use_summary") continue;
        if (Q.type === "result") {
          if (this.firstResultReceived = true, this.firstResultReceivedResolve) this.firstResultReceivedResolve();
          if (this.isSingleUserTurn) v1("[Query.readMessages] First result received for single-turn query, closing stdin"), this.transport.endInput();
        }
        this.inputStream.enqueue(Q);
      }
      if (this.firstResultReceivedResolve) this.firstResultReceivedResolve();
      this.inputStream.done(), this.cleanup();
    } catch (Q) {
      if (this.firstResultReceivedResolve) this.firstResultReceivedResolve();
      this.inputStream.error(Q), this.cleanup(Q);
    }
  }
  async handleControlRequest(Q) {
    let X = new AbortController();
    this.cancelControllers.set(Q.request_id, X);
    try {
      let Y = await this.processControlRequest(Q, X.signal), $ = { type: "control_response", response: { subtype: "success", request_id: Q.request_id, response: Y } };
      await Promise.resolve(this.transport.write(Z0($) + `
`));
    } catch (Y) {
      let $ = { type: "control_response", response: { subtype: "error", request_id: Q.request_id, error: Y.message || String(Y) } };
      await Promise.resolve(this.transport.write(Z0($) + `
`));
    } finally {
      this.cancelControllers.delete(Q.request_id);
    }
  }
  handleControlCancelRequest(Q) {
    let X = this.cancelControllers.get(Q.request_id);
    if (X) X.abort(), this.cancelControllers.delete(Q.request_id);
  }
  async processControlRequest(Q, X) {
    if (Q.request.subtype === "can_use_tool") {
      if (!this.canUseTool) throw Error("canUseTool callback is not provided.");
      return { ...await this.canUseTool(Q.request.tool_name, Q.request.input, { signal: X, suggestions: Q.request.permission_suggestions, blockedPath: Q.request.blocked_path, decisionReason: Q.request.decision_reason, toolUseID: Q.request.tool_use_id, agentID: Q.request.agent_id }), toolUseID: Q.request.tool_use_id };
    } else if (Q.request.subtype === "hook_callback") return await this.handleHookCallbacks(Q.request.callback_id, Q.request.input, Q.request.tool_use_id, X);
    else if (Q.request.subtype === "mcp_message") {
      let Y = Q.request, $ = this.sdkMcpTransports.get(Y.server_name);
      if (!$) throw Error(`SDK MCP server not found: ${Y.server_name}`);
      if ("method" in Y.message && "id" in Y.message && Y.message.id !== null) return { mcp_response: await this.handleMcpControlRequest(Y.server_name, Y, $) };
      else {
        if ($.onmessage) $.onmessage(Y.message);
        return { mcp_response: { jsonrpc: "2.0", result: {}, id: 0 } };
      }
    }
    throw Error("Unsupported control request subtype: " + Q.request.subtype);
  }
  async *readSdkMessages() {
    for await (let Q of this.inputStream) yield Q;
  }
  async initialize() {
    let Q;
    if (this.hooks) {
      Q = {};
      for (let [W, J] of Object.entries(this.hooks)) if (J.length > 0) Q[W] = J.map((G) => {
        let H = [];
        for (let B of G.hooks) {
          let z = `hook_${this.nextCallbackId++}`;
          this.hookCallbacks.set(z, B), H.push(z);
        }
        return { matcher: G.matcher, hookCallbackIds: H, timeout: G.timeout };
      });
    }
    let X = this.sdkMcpTransports.size > 0 ? Array.from(this.sdkMcpTransports.keys()) : void 0, Y = { subtype: "initialize", hooks: Q, sdkMcpServers: X, jsonSchema: this.jsonSchema, systemPrompt: this.initConfig?.systemPrompt, appendSystemPrompt: this.initConfig?.appendSystemPrompt, agents: this.initConfig?.agents };
    return (await this.request(Y)).response;
  }
  async interrupt() {
    await this.request({ subtype: "interrupt" });
  }
  async setPermissionMode(Q) {
    await this.request({ subtype: "set_permission_mode", mode: Q });
  }
  async setModel(Q) {
    await this.request({ subtype: "set_model", model: Q });
  }
  async setMaxThinkingTokens(Q) {
    await this.request({ subtype: "set_max_thinking_tokens", max_thinking_tokens: Q });
  }
  async rewindFiles(Q, X) {
    return (await this.request({ subtype: "rewind_files", user_message_id: Q, dry_run: X?.dryRun })).response;
  }
  async processPendingPermissionRequests(Q) {
    for (let X of Q) if (X.request.subtype === "can_use_tool") this.handleControlRequest(X).catch(() => {
    });
  }
  request(Q) {
    let X = Math.random().toString(36).substring(2, 15), Y = { request_id: X, type: "control_request", request: Q };
    return new Promise(($, W) => {
      this.pendingControlResponses.set(X, { handler: (J) => {
        if (this.pendingControlResponses.delete(X), J.subtype === "success") $(J);
        else if (W(Error(J.error)), J.pending_permission_requests) this.processPendingPermissionRequests(J.pending_permission_requests);
      }, reject: W }), Promise.resolve(this.transport.write(Z0(Y) + `
`));
    });
  }
  async initializationResult() {
    return this.initialization;
  }
  async supportedCommands() {
    return (await this.initialization).commands;
  }
  async supportedModels() {
    return (await this.initialization).models;
  }
  async reconnectMcpServer(Q) {
    await this.request({ subtype: "mcp_reconnect", serverName: Q });
  }
  async toggleMcpServer(Q, X) {
    await this.request({ subtype: "mcp_toggle", serverName: Q, enabled: X });
  }
  async mcpServerStatus() {
    return (await this.request({ subtype: "mcp_status" })).response.mcpServers;
  }
  async setMcpServers(Q) {
    let X = {}, Y = {};
    for (let [H, B] of Object.entries(Q)) if (B.type === "sdk" && "instance" in B) X[H] = B.instance;
    else Y[H] = B;
    let $ = new Set(this.sdkMcpServerInstances.keys()), W = new Set(Object.keys(X));
    for (let H of $) if (!W.has(H)) await this.disconnectSdkMcpServer(H);
    for (let [H, B] of Object.entries(X)) if (!$.has(H)) this.connectSdkMcpServer(H, B);
    let J = {};
    for (let H of Object.keys(X)) J[H] = { type: "sdk", name: H };
    return (await this.request({ subtype: "mcp_set_servers", servers: { ...Y, ...J } })).response;
  }
  async accountInfo() {
    return (await this.initialization).account;
  }
  async streamInput(Q) {
    v1("[Query.streamInput] Starting to process input stream");
    try {
      let X = 0;
      for await (let Y of Q) {
        if (X++, v1(`[Query.streamInput] Processing message ${X}: ${Y.type}`), this.abortController?.signal.aborted) break;
        await Promise.resolve(this.transport.write(Z0(Y) + `
`));
      }
      if (v1(`[Query.streamInput] Finished processing ${X} messages from input stream`), X > 0 && this.hasBidirectionalNeeds()) v1("[Query.streamInput] Has bidirectional needs, waiting for first result"), await this.waitForFirstResult();
      v1("[Query] Calling transport.endInput() to close stdin to CLI process"), this.transport.endInput();
    } catch (X) {
      if (!(X instanceof F1)) throw X;
    }
  }
  waitForFirstResult() {
    if (this.firstResultReceived) return v1("[Query.waitForFirstResult] Result already received, returning immediately"), Promise.resolve();
    return new Promise((Q) => {
      if (this.abortController?.signal.aborted) {
        Q();
        return;
      }
      this.abortController?.signal.addEventListener("abort", () => Q(), { once: true }), this.firstResultReceivedResolve = Q;
    });
  }
  handleHookCallbacks(Q, X, Y, $) {
    let W = this.hookCallbacks.get(Q);
    if (!W) throw Error(`No hook callback found for ID: ${Q}`);
    return W(X, Y, { signal: $ });
  }
  connectSdkMcpServer(Q, X) {
    let Y = new qQ(($) => this.sendMcpServerMessageToCli(Q, $));
    this.sdkMcpTransports.set(Q, Y), this.sdkMcpServerInstances.set(Q, X), X.connect(Y);
  }
  async disconnectSdkMcpServer(Q) {
    let X = this.sdkMcpTransports.get(Q);
    if (X) await X.close(), this.sdkMcpTransports.delete(Q);
    this.sdkMcpServerInstances.delete(Q);
  }
  sendMcpServerMessageToCli(Q, X) {
    if ("id" in X && X.id !== null && X.id !== void 0) {
      let $ = `${Q}:${X.id}`, W = this.pendingMcpResponses.get($);
      if (W) {
        W.resolve(X), this.pendingMcpResponses.delete($);
        return;
      }
    }
    let Y = { type: "control_request", request_id: Hq(), request: { subtype: "mcp_message", server_name: Q, message: X } };
    this.transport.write(Z0(Y) + `
`);
  }
  handleMcpControlRequest(Q, X, Y) {
    let $ = "id" in X.message ? X.message.id : null, W = `${Q}:${$}`;
    return new Promise((J, G) => {
      let H = () => {
        this.pendingMcpResponses.delete(W);
      }, B = (K) => {
        H(), J(K);
      }, z = (K) => {
        H(), G(K);
      };
      if (this.pendingMcpResponses.set(W, { resolve: B, reject: z }), Y.onmessage) Y.onmessage(X.message);
      else {
        H(), G(Error("No message handler registered"));
        return;
      }
    });
  }
};
var n;
(function(Q) {
  Q.assertEqual = (W) => {
  };
  function X(W) {
  }
  Q.assertIs = X;
  function Y(W) {
    throw Error();
  }
  Q.assertNever = Y, Q.arrayToEnum = (W) => {
    let J = {};
    for (let G of W) J[G] = G;
    return J;
  }, Q.getValidEnumValues = (W) => {
    let J = Q.objectKeys(W).filter((H) => typeof W[W[H]] !== "number"), G = {};
    for (let H of J) G[H] = W[H];
    return Q.objectValues(G);
  }, Q.objectValues = (W) => {
    return Q.objectKeys(W).map(function(J) {
      return W[J];
    });
  }, Q.objectKeys = typeof Object.keys === "function" ? (W) => Object.keys(W) : (W) => {
    let J = [];
    for (let G in W) if (Object.prototype.hasOwnProperty.call(W, G)) J.push(G);
    return J;
  }, Q.find = (W, J) => {
    for (let G of W) if (J(G)) return G;
    return;
  }, Q.isInteger = typeof Number.isInteger === "function" ? (W) => Number.isInteger(W) : (W) => typeof W === "number" && Number.isFinite(W) && Math.floor(W) === W;
  function $(W, J = " | ") {
    return W.map((G) => typeof G === "string" ? `'${G}'` : G).join(J);
  }
  Q.joinValues = $, Q.jsonStringifyReplacer = (W, J) => {
    if (typeof J === "bigint") return J.toString();
    return J;
  };
})(n || (n = {}));
var VW;
(function(Q) {
  Q.mergeShapes = (X, Y) => {
    return { ...X, ...Y };
  };
})(VW || (VW = {}));
var I = n.arrayToEnum(["string", "nan", "number", "integer", "float", "boolean", "date", "bigint", "symbol", "function", "undefined", "null", "array", "object", "unknown", "promise", "void", "never", "map", "set"]);
var D1 = (Q) => {
  switch (typeof Q) {
    case "undefined":
      return I.undefined;
    case "string":
      return I.string;
    case "number":
      return Number.isNaN(Q) ? I.nan : I.number;
    case "boolean":
      return I.boolean;
    case "function":
      return I.function;
    case "bigint":
      return I.bigint;
    case "symbol":
      return I.symbol;
    case "object":
      if (Array.isArray(Q)) return I.array;
      if (Q === null) return I.null;
      if (Q.then && typeof Q.then === "function" && Q.catch && typeof Q.catch === "function") return I.promise;
      if (typeof Map < "u" && Q instanceof Map) return I.map;
      if (typeof Set < "u" && Q instanceof Set) return I.set;
      if (typeof Date < "u" && Q instanceof Date) return I.date;
      return I.object;
    default:
      return I.unknown;
  }
};
var M = n.arrayToEnum(["invalid_type", "invalid_literal", "custom", "invalid_union", "invalid_union_discriminator", "invalid_enum_value", "unrecognized_keys", "invalid_arguments", "invalid_return_type", "invalid_date", "invalid_string", "too_small", "too_big", "invalid_intersection_types", "not_multiple_of", "not_finite"]);
var f0 = class _f0 extends Error {
  get errors() {
    return this.issues;
  }
  constructor(Q) {
    super();
    this.issues = [], this.addIssue = (Y) => {
      this.issues = [...this.issues, Y];
    }, this.addIssues = (Y = []) => {
      this.issues = [...this.issues, ...Y];
    };
    let X = new.target.prototype;
    if (Object.setPrototypeOf) Object.setPrototypeOf(this, X);
    else this.__proto__ = X;
    this.name = "ZodError", this.issues = Q;
  }
  format(Q) {
    let X = Q || function(W) {
      return W.message;
    }, Y = { _errors: [] }, $ = (W) => {
      for (let J of W.issues) if (J.code === "invalid_union") J.unionErrors.map($);
      else if (J.code === "invalid_return_type") $(J.returnTypeError);
      else if (J.code === "invalid_arguments") $(J.argumentsError);
      else if (J.path.length === 0) Y._errors.push(X(J));
      else {
        let G = Y, H = 0;
        while (H < J.path.length) {
          let B = J.path[H];
          if (H !== J.path.length - 1) G[B] = G[B] || { _errors: [] };
          else G[B] = G[B] || { _errors: [] }, G[B]._errors.push(X(J));
          G = G[B], H++;
        }
      }
    };
    return $(this), Y;
  }
  static assert(Q) {
    if (!(Q instanceof _f0)) throw Error(`Not a ZodError: ${Q}`);
  }
  toString() {
    return this.message;
  }
  get message() {
    return JSON.stringify(this.issues, n.jsonStringifyReplacer, 2);
  }
  get isEmpty() {
    return this.issues.length === 0;
  }
  flatten(Q = (X) => X.message) {
    let X = {}, Y = [];
    for (let $ of this.issues) if ($.path.length > 0) {
      let W = $.path[0];
      X[W] = X[W] || [], X[W].push(Q($));
    } else Y.push(Q($));
    return { formErrors: Y, fieldErrors: X };
  }
  get formErrors() {
    return this.flatten();
  }
};
f0.create = (Q) => {
  return new f0(Q);
};
var zq = (Q, X) => {
  let Y;
  switch (Q.code) {
    case M.invalid_type:
      if (Q.received === I.undefined) Y = "Required";
      else Y = `Expected ${Q.expected}, received ${Q.received}`;
      break;
    case M.invalid_literal:
      Y = `Invalid literal value, expected ${JSON.stringify(Q.expected, n.jsonStringifyReplacer)}`;
      break;
    case M.unrecognized_keys:
      Y = `Unrecognized key(s) in object: ${n.joinValues(Q.keys, ", ")}`;
      break;
    case M.invalid_union:
      Y = "Invalid input";
      break;
    case M.invalid_union_discriminator:
      Y = `Invalid discriminator value. Expected ${n.joinValues(Q.options)}`;
      break;
    case M.invalid_enum_value:
      Y = `Invalid enum value. Expected ${n.joinValues(Q.options)}, received '${Q.received}'`;
      break;
    case M.invalid_arguments:
      Y = "Invalid function arguments";
      break;
    case M.invalid_return_type:
      Y = "Invalid function return type";
      break;
    case M.invalid_date:
      Y = "Invalid date";
      break;
    case M.invalid_string:
      if (typeof Q.validation === "object") if ("includes" in Q.validation) {
        if (Y = `Invalid input: must include "${Q.validation.includes}"`, typeof Q.validation.position === "number") Y = `${Y} at one or more positions greater than or equal to ${Q.validation.position}`;
      } else if ("startsWith" in Q.validation) Y = `Invalid input: must start with "${Q.validation.startsWith}"`;
      else if ("endsWith" in Q.validation) Y = `Invalid input: must end with "${Q.validation.endsWith}"`;
      else n.assertNever(Q.validation);
      else if (Q.validation !== "regex") Y = `Invalid ${Q.validation}`;
      else Y = "Invalid";
      break;
    case M.too_small:
      if (Q.type === "array") Y = `Array must contain ${Q.exact ? "exactly" : Q.inclusive ? "at least" : "more than"} ${Q.minimum} element(s)`;
      else if (Q.type === "string") Y = `String must contain ${Q.exact ? "exactly" : Q.inclusive ? "at least" : "over"} ${Q.minimum} character(s)`;
      else if (Q.type === "number") Y = `Number must be ${Q.exact ? "exactly equal to " : Q.inclusive ? "greater than or equal to " : "greater than "}${Q.minimum}`;
      else if (Q.type === "bigint") Y = `Number must be ${Q.exact ? "exactly equal to " : Q.inclusive ? "greater than or equal to " : "greater than "}${Q.minimum}`;
      else if (Q.type === "date") Y = `Date must be ${Q.exact ? "exactly equal to " : Q.inclusive ? "greater than or equal to " : "greater than "}${new Date(Number(Q.minimum))}`;
      else Y = "Invalid input";
      break;
    case M.too_big:
      if (Q.type === "array") Y = `Array must contain ${Q.exact ? "exactly" : Q.inclusive ? "at most" : "less than"} ${Q.maximum} element(s)`;
      else if (Q.type === "string") Y = `String must contain ${Q.exact ? "exactly" : Q.inclusive ? "at most" : "under"} ${Q.maximum} character(s)`;
      else if (Q.type === "number") Y = `Number must be ${Q.exact ? "exactly" : Q.inclusive ? "less than or equal to" : "less than"} ${Q.maximum}`;
      else if (Q.type === "bigint") Y = `BigInt must be ${Q.exact ? "exactly" : Q.inclusive ? "less than or equal to" : "less than"} ${Q.maximum}`;
      else if (Q.type === "date") Y = `Date must be ${Q.exact ? "exactly" : Q.inclusive ? "smaller than or equal to" : "smaller than"} ${new Date(Number(Q.maximum))}`;
      else Y = "Invalid input";
      break;
    case M.custom:
      Y = "Invalid input";
      break;
    case M.invalid_intersection_types:
      Y = "Intersection results could not be merged";
      break;
    case M.not_multiple_of:
      Y = `Number must be a multiple of ${Q.multipleOf}`;
      break;
    case M.not_finite:
      Y = "Number must be finite";
      break;
    default:
      Y = X.defaultError, n.assertNever(Q);
  }
  return { message: Y };
};
var _1 = zq;
var Kq = _1;
function $4() {
  return Kq;
}
var D9 = (Q) => {
  let { data: X, path: Y, errorMaps: $, issueData: W } = Q, J = [...Y, ...W.path || []], G = { ...W, path: J };
  if (W.message !== void 0) return { ...W, path: J, message: W.message };
  let H = "", B = $.filter((z) => !!z).slice().reverse();
  for (let z of B) H = z(G, { data: X, defaultError: H }).message;
  return { ...W, path: J, message: H };
};
function b(Q, X) {
  let Y = $4(), $ = D9({ issueData: X, data: Q.data, path: Q.path, errorMaps: [Q.common.contextualErrorMap, Q.schemaErrorMap, Y, Y === _1 ? void 0 : _1].filter((W) => !!W) });
  Q.common.issues.push($);
}
var b0 = class _b0 {
  constructor() {
    this.value = "valid";
  }
  dirty() {
    if (this.value === "valid") this.value = "dirty";
  }
  abort() {
    if (this.value !== "aborted") this.value = "aborted";
  }
  static mergeArray(Q, X) {
    let Y = [];
    for (let $ of X) {
      if ($.status === "aborted") return g;
      if ($.status === "dirty") Q.dirty();
      Y.push($.value);
    }
    return { status: Q.value, value: Y };
  }
  static async mergeObjectAsync(Q, X) {
    let Y = [];
    for (let $ of X) {
      let W = await $.key, J = await $.value;
      Y.push({ key: W, value: J });
    }
    return _b0.mergeObjectSync(Q, Y);
  }
  static mergeObjectSync(Q, X) {
    let Y = {};
    for (let $ of X) {
      let { key: W, value: J } = $;
      if (W.status === "aborted") return g;
      if (J.status === "aborted") return g;
      if (W.status === "dirty") Q.dirty();
      if (J.status === "dirty") Q.dirty();
      if (W.value !== "__proto__" && (typeof J.value < "u" || $.alwaysSet)) Y[W.value] = J.value;
    }
    return { status: Q.value, value: Y };
  }
};
var g = Object.freeze({ status: "aborted" });
var I6 = (Q) => ({ status: "dirty", value: Q });
var C0 = (Q) => ({ status: "valid", value: Q });
var FQ = (Q) => Q.status === "aborted";
var NQ = (Q) => Q.status === "dirty";
var a1 = (Q) => Q.status === "valid";
var W4 = (Q) => typeof Promise < "u" && Q instanceof Promise;
var Z;
(function(Q) {
  Q.errToObj = (X) => typeof X === "string" ? { message: X } : X || {}, Q.toString = (X) => typeof X === "string" ? X : X?.message;
})(Z || (Z = {}));
var n0 = class {
  constructor(Q, X, Y, $) {
    this._cachedPath = [], this.parent = Q, this.data = X, this._path = Y, this._key = $;
  }
  get path() {
    if (!this._cachedPath.length) if (Array.isArray(this._key)) this._cachedPath.push(...this._path, ...this._key);
    else this._cachedPath.push(...this._path, this._key);
    return this._cachedPath;
  }
};
var qW = (Q, X) => {
  if (a1(X)) return { success: true, data: X.value };
  else {
    if (!Q.common.issues.length) throw Error("Validation failed but no issues detected.");
    return { success: false, get error() {
      if (this._error) return this._error;
      let Y = new f0(Q.common.issues);
      return this._error = Y, this._error;
    } };
  }
};
function l(Q) {
  if (!Q) return {};
  let { errorMap: X, invalid_type_error: Y, required_error: $, description: W } = Q;
  if (X && (Y || $)) throw Error(`Can't use "invalid_type_error" or "required_error" in conjunction with custom error map.`);
  if (X) return { errorMap: X, description: W };
  return { errorMap: (G, H) => {
    let { message: B } = Q;
    if (G.code === "invalid_enum_value") return { message: B ?? H.defaultError };
    if (typeof H.data > "u") return { message: B ?? $ ?? H.defaultError };
    if (G.code !== "invalid_type") return { message: H.defaultError };
    return { message: B ?? Y ?? H.defaultError };
  }, description: W };
}
var d = class {
  get description() {
    return this._def.description;
  }
  _getType(Q) {
    return D1(Q.data);
  }
  _getOrReturnCtx(Q, X) {
    return X || { common: Q.parent.common, data: Q.data, parsedType: D1(Q.data), schemaErrorMap: this._def.errorMap, path: Q.path, parent: Q.parent };
  }
  _processInputParams(Q) {
    return { status: new b0(), ctx: { common: Q.parent.common, data: Q.data, parsedType: D1(Q.data), schemaErrorMap: this._def.errorMap, path: Q.path, parent: Q.parent } };
  }
  _parseSync(Q) {
    let X = this._parse(Q);
    if (W4(X)) throw Error("Synchronous parse encountered promise.");
    return X;
  }
  _parseAsync(Q) {
    let X = this._parse(Q);
    return Promise.resolve(X);
  }
  parse(Q, X) {
    let Y = this.safeParse(Q, X);
    if (Y.success) return Y.data;
    throw Y.error;
  }
  safeParse(Q, X) {
    let Y = { common: { issues: [], async: X?.async ?? false, contextualErrorMap: X?.errorMap }, path: X?.path || [], schemaErrorMap: this._def.errorMap, parent: null, data: Q, parsedType: D1(Q) }, $ = this._parseSync({ data: Q, path: Y.path, parent: Y });
    return qW(Y, $);
  }
  "~validate"(Q) {
    let X = { common: { issues: [], async: !!this["~standard"].async }, path: [], schemaErrorMap: this._def.errorMap, parent: null, data: Q, parsedType: D1(Q) };
    if (!this["~standard"].async) try {
      let Y = this._parseSync({ data: Q, path: [], parent: X });
      return a1(Y) ? { value: Y.value } : { issues: X.common.issues };
    } catch (Y) {
      if (Y?.message?.toLowerCase()?.includes("encountered")) this["~standard"].async = true;
      X.common = { issues: [], async: true };
    }
    return this._parseAsync({ data: Q, path: [], parent: X }).then((Y) => a1(Y) ? { value: Y.value } : { issues: X.common.issues });
  }
  async parseAsync(Q, X) {
    let Y = await this.safeParseAsync(Q, X);
    if (Y.success) return Y.data;
    throw Y.error;
  }
  async safeParseAsync(Q, X) {
    let Y = { common: { issues: [], contextualErrorMap: X?.errorMap, async: true }, path: X?.path || [], schemaErrorMap: this._def.errorMap, parent: null, data: Q, parsedType: D1(Q) }, $ = this._parse({ data: Q, path: Y.path, parent: Y }), W = await (W4($) ? $ : Promise.resolve($));
    return qW(Y, W);
  }
  refine(Q, X) {
    let Y = ($) => {
      if (typeof X === "string" || typeof X > "u") return { message: X };
      else if (typeof X === "function") return X($);
      else return X;
    };
    return this._refinement(($, W) => {
      let J = Q($), G = () => W.addIssue({ code: M.custom, ...Y($) });
      if (typeof Promise < "u" && J instanceof Promise) return J.then((H) => {
        if (!H) return G(), false;
        else return true;
      });
      if (!J) return G(), false;
      else return true;
    });
  }
  refinement(Q, X) {
    return this._refinement((Y, $) => {
      if (!Q(Y)) return $.addIssue(typeof X === "function" ? X(Y, $) : X), false;
      else return true;
    });
  }
  _refinement(Q) {
    return new G1({ schema: this, typeName: j.ZodEffects, effect: { type: "refinement", refinement: Q } });
  }
  superRefine(Q) {
    return this._refinement(Q);
  }
  constructor(Q) {
    this.spa = this.safeParseAsync, this._def = Q, this.parse = this.parse.bind(this), this.safeParse = this.safeParse.bind(this), this.parseAsync = this.parseAsync.bind(this), this.safeParseAsync = this.safeParseAsync.bind(this), this.spa = this.spa.bind(this), this.refine = this.refine.bind(this), this.refinement = this.refinement.bind(this), this.superRefine = this.superRefine.bind(this), this.optional = this.optional.bind(this), this.nullable = this.nullable.bind(this), this.nullish = this.nullish.bind(this), this.array = this.array.bind(this), this.promise = this.promise.bind(this), this.or = this.or.bind(this), this.and = this.and.bind(this), this.transform = this.transform.bind(this), this.brand = this.brand.bind(this), this.default = this.default.bind(this), this.catch = this.catch.bind(this), this.describe = this.describe.bind(this), this.pipe = this.pipe.bind(this), this.readonly = this.readonly.bind(this), this.isNullable = this.isNullable.bind(this), this.isOptional = this.isOptional.bind(this), this["~standard"] = { version: 1, vendor: "zod", validate: (X) => this["~validate"](X) };
  }
  optional() {
    return J1.create(this, this._def);
  }
  nullable() {
    return T1.create(this, this._def);
  }
  nullish() {
    return this.nullable().optional();
  }
  array() {
    return W1.create(this);
  }
  promise() {
    return Z6.create(this, this._def);
  }
  or(Q) {
    return z4.create([this, Q], this._def);
  }
  and(Q) {
    return K4.create(this, Q, this._def);
  }
  transform(Q) {
    return new G1({ ...l(this._def), schema: this, typeName: j.ZodEffects, effect: { type: "transform", transform: Q } });
  }
  default(Q) {
    let X = typeof Q === "function" ? Q : () => Q;
    return new L4({ ...l(this._def), innerType: this, defaultValue: X, typeName: j.ZodDefault });
  }
  brand() {
    return new MQ({ typeName: j.ZodBranded, type: this, ...l(this._def) });
  }
  catch(Q) {
    let X = typeof Q === "function" ? Q : () => Q;
    return new F4({ ...l(this._def), innerType: this, catchValue: X, typeName: j.ZodCatch });
  }
  describe(Q) {
    return new this.constructor({ ...this._def, description: Q });
  }
  pipe(Q) {
    return E9.create(this, Q);
  }
  readonly() {
    return N4.create(this);
  }
  isOptional() {
    return this.safeParse(void 0).success;
  }
  isNullable() {
    return this.safeParse(null).success;
  }
};
var Vq = /^c[^\s-]{8,}$/i;
var qq = /^[0-9a-z]+$/;
var Uq = /^[0-9A-HJKMNP-TV-Z]{26}$/i;
var Lq = /^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$/i;
var Fq = /^[a-z0-9_-]{21}$/i;
var Nq = /^[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]*$/;
var Dq = /^[-+]?P(?!$)(?:(?:[-+]?\d+Y)|(?:[-+]?\d+[.,]\d+Y$))?(?:(?:[-+]?\d+M)|(?:[-+]?\d+[.,]\d+M$))?(?:(?:[-+]?\d+W)|(?:[-+]?\d+[.,]\d+W$))?(?:(?:[-+]?\d+D)|(?:[-+]?\d+[.,]\d+D$))?(?:T(?=[\d+-])(?:(?:[-+]?\d+H)|(?:[-+]?\d+[.,]\d+H$))?(?:(?:[-+]?\d+M)|(?:[-+]?\d+[.,]\d+M$))?(?:[-+]?\d+(?:[.,]\d+)?S)?)??$/;
var Oq = /^(?!\.)(?!.*\.\.)([A-Z0-9_'+\-\.]*)[A-Z0-9_+-]@([A-Z0-9][A-Z0-9\-]*\.)+[A-Z]{2,}$/i;
var wq = "^(\\p{Extended_Pictographic}|\\p{Emoji_Component})+$";
var DQ;
var Mq = /^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$/;
var Aq = /^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\/(3[0-2]|[12]?[0-9])$/;
var jq = /^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$/;
var Rq = /^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))\/(12[0-8]|1[01][0-9]|[1-9]?[0-9])$/;
var Iq = /^([0-9a-zA-Z+/]{4})*(([0-9a-zA-Z+/]{2}==)|([0-9a-zA-Z+/]{3}=))?$/;
var Eq = /^([0-9a-zA-Z-_]{4})*(([0-9a-zA-Z-_]{2}(==)?)|([0-9a-zA-Z-_]{3}(=)?))?$/;
var UW = "((\\d\\d[2468][048]|\\d\\d[13579][26]|\\d\\d0[48]|[02468][048]00|[13579][26]00)-02-29|\\d{4}-((0[13578]|1[02])-(0[1-9]|[12]\\d|3[01])|(0[469]|11)-(0[1-9]|[12]\\d|30)|(02)-(0[1-9]|1\\d|2[0-8])))";
var bq = new RegExp(`^${UW}$`);
function LW(Q) {
  let X = "[0-5]\\d";
  if (Q.precision) X = `${X}\\.\\d{${Q.precision}}`;
  else if (Q.precision == null) X = `${X}(\\.\\d+)?`;
  let Y = Q.precision ? "+" : "?";
  return `([01]\\d|2[0-3]):[0-5]\\d(:${X})${Y}`;
}
function Pq(Q) {
  return new RegExp(`^${LW(Q)}$`);
}
function Sq(Q) {
  let X = `${UW}T${LW(Q)}`, Y = [];
  if (Y.push(Q.local ? "Z?" : "Z"), Q.offset) Y.push("([+-]\\d{2}:?\\d{2})");
  return X = `${X}(${Y.join("|")})`, new RegExp(`^${X}$`);
}
function Zq(Q, X) {
  if ((X === "v4" || !X) && Mq.test(Q)) return true;
  if ((X === "v6" || !X) && jq.test(Q)) return true;
  return false;
}
function Cq(Q, X) {
  if (!Nq.test(Q)) return false;
  try {
    let [Y] = Q.split(".");
    if (!Y) return false;
    let $ = Y.replace(/-/g, "+").replace(/_/g, "/").padEnd(Y.length + (4 - Y.length % 4) % 4, "="), W = JSON.parse(atob($));
    if (typeof W !== "object" || W === null) return false;
    if ("typ" in W && W?.typ !== "JWT") return false;
    if (!W.alg) return false;
    if (X && W.alg !== X) return false;
    return true;
  } catch {
    return false;
  }
}
function kq(Q, X) {
  if ((X === "v4" || !X) && Aq.test(Q)) return true;
  if ((X === "v6" || !X) && Rq.test(Q)) return true;
  return false;
}
var w1 = class _w1 extends d {
  _parse(Q) {
    if (this._def.coerce) Q.data = String(Q.data);
    if (this._getType(Q) !== I.string) {
      let W = this._getOrReturnCtx(Q);
      return b(W, { code: M.invalid_type, expected: I.string, received: W.parsedType }), g;
    }
    let Y = new b0(), $ = void 0;
    for (let W of this._def.checks) if (W.kind === "min") {
      if (Q.data.length < W.value) $ = this._getOrReturnCtx(Q, $), b($, { code: M.too_small, minimum: W.value, type: "string", inclusive: true, exact: false, message: W.message }), Y.dirty();
    } else if (W.kind === "max") {
      if (Q.data.length > W.value) $ = this._getOrReturnCtx(Q, $), b($, { code: M.too_big, maximum: W.value, type: "string", inclusive: true, exact: false, message: W.message }), Y.dirty();
    } else if (W.kind === "length") {
      let J = Q.data.length > W.value, G = Q.data.length < W.value;
      if (J || G) {
        if ($ = this._getOrReturnCtx(Q, $), J) b($, { code: M.too_big, maximum: W.value, type: "string", inclusive: true, exact: true, message: W.message });
        else if (G) b($, { code: M.too_small, minimum: W.value, type: "string", inclusive: true, exact: true, message: W.message });
        Y.dirty();
      }
    } else if (W.kind === "email") {
      if (!Oq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "email", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "emoji") {
      if (!DQ) DQ = new RegExp(wq, "u");
      if (!DQ.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "emoji", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "uuid") {
      if (!Lq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "uuid", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "nanoid") {
      if (!Fq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "nanoid", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "cuid") {
      if (!Vq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "cuid", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "cuid2") {
      if (!qq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "cuid2", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "ulid") {
      if (!Uq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "ulid", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "url") try {
      new URL(Q.data);
    } catch {
      $ = this._getOrReturnCtx(Q, $), b($, { validation: "url", code: M.invalid_string, message: W.message }), Y.dirty();
    }
    else if (W.kind === "regex") {
      if (W.regex.lastIndex = 0, !W.regex.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "regex", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "trim") Q.data = Q.data.trim();
    else if (W.kind === "includes") {
      if (!Q.data.includes(W.value, W.position)) $ = this._getOrReturnCtx(Q, $), b($, { code: M.invalid_string, validation: { includes: W.value, position: W.position }, message: W.message }), Y.dirty();
    } else if (W.kind === "toLowerCase") Q.data = Q.data.toLowerCase();
    else if (W.kind === "toUpperCase") Q.data = Q.data.toUpperCase();
    else if (W.kind === "startsWith") {
      if (!Q.data.startsWith(W.value)) $ = this._getOrReturnCtx(Q, $), b($, { code: M.invalid_string, validation: { startsWith: W.value }, message: W.message }), Y.dirty();
    } else if (W.kind === "endsWith") {
      if (!Q.data.endsWith(W.value)) $ = this._getOrReturnCtx(Q, $), b($, { code: M.invalid_string, validation: { endsWith: W.value }, message: W.message }), Y.dirty();
    } else if (W.kind === "datetime") {
      if (!Sq(W).test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { code: M.invalid_string, validation: "datetime", message: W.message }), Y.dirty();
    } else if (W.kind === "date") {
      if (!bq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { code: M.invalid_string, validation: "date", message: W.message }), Y.dirty();
    } else if (W.kind === "time") {
      if (!Pq(W).test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { code: M.invalid_string, validation: "time", message: W.message }), Y.dirty();
    } else if (W.kind === "duration") {
      if (!Dq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "duration", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "ip") {
      if (!Zq(Q.data, W.version)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "ip", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "jwt") {
      if (!Cq(Q.data, W.alg)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "jwt", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "cidr") {
      if (!kq(Q.data, W.version)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "cidr", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "base64") {
      if (!Iq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "base64", code: M.invalid_string, message: W.message }), Y.dirty();
    } else if (W.kind === "base64url") {
      if (!Eq.test(Q.data)) $ = this._getOrReturnCtx(Q, $), b($, { validation: "base64url", code: M.invalid_string, message: W.message }), Y.dirty();
    } else n.assertNever(W);
    return { status: Y.value, value: Q.data };
  }
  _regex(Q, X, Y) {
    return this.refinement(($) => Q.test($), { validation: X, code: M.invalid_string, ...Z.errToObj(Y) });
  }
  _addCheck(Q) {
    return new _w1({ ...this._def, checks: [...this._def.checks, Q] });
  }
  email(Q) {
    return this._addCheck({ kind: "email", ...Z.errToObj(Q) });
  }
  url(Q) {
    return this._addCheck({ kind: "url", ...Z.errToObj(Q) });
  }
  emoji(Q) {
    return this._addCheck({ kind: "emoji", ...Z.errToObj(Q) });
  }
  uuid(Q) {
    return this._addCheck({ kind: "uuid", ...Z.errToObj(Q) });
  }
  nanoid(Q) {
    return this._addCheck({ kind: "nanoid", ...Z.errToObj(Q) });
  }
  cuid(Q) {
    return this._addCheck({ kind: "cuid", ...Z.errToObj(Q) });
  }
  cuid2(Q) {
    return this._addCheck({ kind: "cuid2", ...Z.errToObj(Q) });
  }
  ulid(Q) {
    return this._addCheck({ kind: "ulid", ...Z.errToObj(Q) });
  }
  base64(Q) {
    return this._addCheck({ kind: "base64", ...Z.errToObj(Q) });
  }
  base64url(Q) {
    return this._addCheck({ kind: "base64url", ...Z.errToObj(Q) });
  }
  jwt(Q) {
    return this._addCheck({ kind: "jwt", ...Z.errToObj(Q) });
  }
  ip(Q) {
    return this._addCheck({ kind: "ip", ...Z.errToObj(Q) });
  }
  cidr(Q) {
    return this._addCheck({ kind: "cidr", ...Z.errToObj(Q) });
  }
  datetime(Q) {
    if (typeof Q === "string") return this._addCheck({ kind: "datetime", precision: null, offset: false, local: false, message: Q });
    return this._addCheck({ kind: "datetime", precision: typeof Q?.precision > "u" ? null : Q?.precision, offset: Q?.offset ?? false, local: Q?.local ?? false, ...Z.errToObj(Q?.message) });
  }
  date(Q) {
    return this._addCheck({ kind: "date", message: Q });
  }
  time(Q) {
    if (typeof Q === "string") return this._addCheck({ kind: "time", precision: null, message: Q });
    return this._addCheck({ kind: "time", precision: typeof Q?.precision > "u" ? null : Q?.precision, ...Z.errToObj(Q?.message) });
  }
  duration(Q) {
    return this._addCheck({ kind: "duration", ...Z.errToObj(Q) });
  }
  regex(Q, X) {
    return this._addCheck({ kind: "regex", regex: Q, ...Z.errToObj(X) });
  }
  includes(Q, X) {
    return this._addCheck({ kind: "includes", value: Q, position: X?.position, ...Z.errToObj(X?.message) });
  }
  startsWith(Q, X) {
    return this._addCheck({ kind: "startsWith", value: Q, ...Z.errToObj(X) });
  }
  endsWith(Q, X) {
    return this._addCheck({ kind: "endsWith", value: Q, ...Z.errToObj(X) });
  }
  min(Q, X) {
    return this._addCheck({ kind: "min", value: Q, ...Z.errToObj(X) });
  }
  max(Q, X) {
    return this._addCheck({ kind: "max", value: Q, ...Z.errToObj(X) });
  }
  length(Q, X) {
    return this._addCheck({ kind: "length", value: Q, ...Z.errToObj(X) });
  }
  nonempty(Q) {
    return this.min(1, Z.errToObj(Q));
  }
  trim() {
    return new _w1({ ...this._def, checks: [...this._def.checks, { kind: "trim" }] });
  }
  toLowerCase() {
    return new _w1({ ...this._def, checks: [...this._def.checks, { kind: "toLowerCase" }] });
  }
  toUpperCase() {
    return new _w1({ ...this._def, checks: [...this._def.checks, { kind: "toUpperCase" }] });
  }
  get isDatetime() {
    return !!this._def.checks.find((Q) => Q.kind === "datetime");
  }
  get isDate() {
    return !!this._def.checks.find((Q) => Q.kind === "date");
  }
  get isTime() {
    return !!this._def.checks.find((Q) => Q.kind === "time");
  }
  get isDuration() {
    return !!this._def.checks.find((Q) => Q.kind === "duration");
  }
  get isEmail() {
    return !!this._def.checks.find((Q) => Q.kind === "email");
  }
  get isURL() {
    return !!this._def.checks.find((Q) => Q.kind === "url");
  }
  get isEmoji() {
    return !!this._def.checks.find((Q) => Q.kind === "emoji");
  }
  get isUUID() {
    return !!this._def.checks.find((Q) => Q.kind === "uuid");
  }
  get isNANOID() {
    return !!this._def.checks.find((Q) => Q.kind === "nanoid");
  }
  get isCUID() {
    return !!this._def.checks.find((Q) => Q.kind === "cuid");
  }
  get isCUID2() {
    return !!this._def.checks.find((Q) => Q.kind === "cuid2");
  }
  get isULID() {
    return !!this._def.checks.find((Q) => Q.kind === "ulid");
  }
  get isIP() {
    return !!this._def.checks.find((Q) => Q.kind === "ip");
  }
  get isCIDR() {
    return !!this._def.checks.find((Q) => Q.kind === "cidr");
  }
  get isBase64() {
    return !!this._def.checks.find((Q) => Q.kind === "base64");
  }
  get isBase64url() {
    return !!this._def.checks.find((Q) => Q.kind === "base64url");
  }
  get minLength() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "min") {
      if (Q === null || X.value > Q) Q = X.value;
    }
    return Q;
  }
  get maxLength() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "max") {
      if (Q === null || X.value < Q) Q = X.value;
    }
    return Q;
  }
};
w1.create = (Q) => {
  return new w1({ checks: [], typeName: j.ZodString, coerce: Q?.coerce ?? false, ...l(Q) });
};
function vq(Q, X) {
  let Y = (Q.toString().split(".")[1] || "").length, $ = (X.toString().split(".")[1] || "").length, W = Y > $ ? Y : $, J = Number.parseInt(Q.toFixed(W).replace(".", "")), G = Number.parseInt(X.toFixed(W).replace(".", ""));
  return J % G / 10 ** W;
}
var b6 = class _b6 extends d {
  constructor() {
    super(...arguments);
    this.min = this.gte, this.max = this.lte, this.step = this.multipleOf;
  }
  _parse(Q) {
    if (this._def.coerce) Q.data = Number(Q.data);
    if (this._getType(Q) !== I.number) {
      let W = this._getOrReturnCtx(Q);
      return b(W, { code: M.invalid_type, expected: I.number, received: W.parsedType }), g;
    }
    let Y = void 0, $ = new b0();
    for (let W of this._def.checks) if (W.kind === "int") {
      if (!n.isInteger(Q.data)) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.invalid_type, expected: "integer", received: "float", message: W.message }), $.dirty();
    } else if (W.kind === "min") {
      if (W.inclusive ? Q.data < W.value : Q.data <= W.value) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.too_small, minimum: W.value, type: "number", inclusive: W.inclusive, exact: false, message: W.message }), $.dirty();
    } else if (W.kind === "max") {
      if (W.inclusive ? Q.data > W.value : Q.data >= W.value) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.too_big, maximum: W.value, type: "number", inclusive: W.inclusive, exact: false, message: W.message }), $.dirty();
    } else if (W.kind === "multipleOf") {
      if (vq(Q.data, W.value) !== 0) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.not_multiple_of, multipleOf: W.value, message: W.message }), $.dirty();
    } else if (W.kind === "finite") {
      if (!Number.isFinite(Q.data)) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.not_finite, message: W.message }), $.dirty();
    } else n.assertNever(W);
    return { status: $.value, value: Q.data };
  }
  gte(Q, X) {
    return this.setLimit("min", Q, true, Z.toString(X));
  }
  gt(Q, X) {
    return this.setLimit("min", Q, false, Z.toString(X));
  }
  lte(Q, X) {
    return this.setLimit("max", Q, true, Z.toString(X));
  }
  lt(Q, X) {
    return this.setLimit("max", Q, false, Z.toString(X));
  }
  setLimit(Q, X, Y, $) {
    return new _b6({ ...this._def, checks: [...this._def.checks, { kind: Q, value: X, inclusive: Y, message: Z.toString($) }] });
  }
  _addCheck(Q) {
    return new _b6({ ...this._def, checks: [...this._def.checks, Q] });
  }
  int(Q) {
    return this._addCheck({ kind: "int", message: Z.toString(Q) });
  }
  positive(Q) {
    return this._addCheck({ kind: "min", value: 0, inclusive: false, message: Z.toString(Q) });
  }
  negative(Q) {
    return this._addCheck({ kind: "max", value: 0, inclusive: false, message: Z.toString(Q) });
  }
  nonpositive(Q) {
    return this._addCheck({ kind: "max", value: 0, inclusive: true, message: Z.toString(Q) });
  }
  nonnegative(Q) {
    return this._addCheck({ kind: "min", value: 0, inclusive: true, message: Z.toString(Q) });
  }
  multipleOf(Q, X) {
    return this._addCheck({ kind: "multipleOf", value: Q, message: Z.toString(X) });
  }
  finite(Q) {
    return this._addCheck({ kind: "finite", message: Z.toString(Q) });
  }
  safe(Q) {
    return this._addCheck({ kind: "min", inclusive: true, value: Number.MIN_SAFE_INTEGER, message: Z.toString(Q) })._addCheck({ kind: "max", inclusive: true, value: Number.MAX_SAFE_INTEGER, message: Z.toString(Q) });
  }
  get minValue() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "min") {
      if (Q === null || X.value > Q) Q = X.value;
    }
    return Q;
  }
  get maxValue() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "max") {
      if (Q === null || X.value < Q) Q = X.value;
    }
    return Q;
  }
  get isInt() {
    return !!this._def.checks.find((Q) => Q.kind === "int" || Q.kind === "multipleOf" && n.isInteger(Q.value));
  }
  get isFinite() {
    let Q = null, X = null;
    for (let Y of this._def.checks) if (Y.kind === "finite" || Y.kind === "int" || Y.kind === "multipleOf") return true;
    else if (Y.kind === "min") {
      if (X === null || Y.value > X) X = Y.value;
    } else if (Y.kind === "max") {
      if (Q === null || Y.value < Q) Q = Y.value;
    }
    return Number.isFinite(X) && Number.isFinite(Q);
  }
};
b6.create = (Q) => {
  return new b6({ checks: [], typeName: j.ZodNumber, coerce: Q?.coerce || false, ...l(Q) });
};
var P6 = class _P6 extends d {
  constructor() {
    super(...arguments);
    this.min = this.gte, this.max = this.lte;
  }
  _parse(Q) {
    if (this._def.coerce) try {
      Q.data = BigInt(Q.data);
    } catch {
      return this._getInvalidInput(Q);
    }
    if (this._getType(Q) !== I.bigint) return this._getInvalidInput(Q);
    let Y = void 0, $ = new b0();
    for (let W of this._def.checks) if (W.kind === "min") {
      if (W.inclusive ? Q.data < W.value : Q.data <= W.value) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.too_small, type: "bigint", minimum: W.value, inclusive: W.inclusive, message: W.message }), $.dirty();
    } else if (W.kind === "max") {
      if (W.inclusive ? Q.data > W.value : Q.data >= W.value) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.too_big, type: "bigint", maximum: W.value, inclusive: W.inclusive, message: W.message }), $.dirty();
    } else if (W.kind === "multipleOf") {
      if (Q.data % W.value !== BigInt(0)) Y = this._getOrReturnCtx(Q, Y), b(Y, { code: M.not_multiple_of, multipleOf: W.value, message: W.message }), $.dirty();
    } else n.assertNever(W);
    return { status: $.value, value: Q.data };
  }
  _getInvalidInput(Q) {
    let X = this._getOrReturnCtx(Q);
    return b(X, { code: M.invalid_type, expected: I.bigint, received: X.parsedType }), g;
  }
  gte(Q, X) {
    return this.setLimit("min", Q, true, Z.toString(X));
  }
  gt(Q, X) {
    return this.setLimit("min", Q, false, Z.toString(X));
  }
  lte(Q, X) {
    return this.setLimit("max", Q, true, Z.toString(X));
  }
  lt(Q, X) {
    return this.setLimit("max", Q, false, Z.toString(X));
  }
  setLimit(Q, X, Y, $) {
    return new _P6({ ...this._def, checks: [...this._def.checks, { kind: Q, value: X, inclusive: Y, message: Z.toString($) }] });
  }
  _addCheck(Q) {
    return new _P6({ ...this._def, checks: [...this._def.checks, Q] });
  }
  positive(Q) {
    return this._addCheck({ kind: "min", value: BigInt(0), inclusive: false, message: Z.toString(Q) });
  }
  negative(Q) {
    return this._addCheck({ kind: "max", value: BigInt(0), inclusive: false, message: Z.toString(Q) });
  }
  nonpositive(Q) {
    return this._addCheck({ kind: "max", value: BigInt(0), inclusive: true, message: Z.toString(Q) });
  }
  nonnegative(Q) {
    return this._addCheck({ kind: "min", value: BigInt(0), inclusive: true, message: Z.toString(Q) });
  }
  multipleOf(Q, X) {
    return this._addCheck({ kind: "multipleOf", value: Q, message: Z.toString(X) });
  }
  get minValue() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "min") {
      if (Q === null || X.value > Q) Q = X.value;
    }
    return Q;
  }
  get maxValue() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "max") {
      if (Q === null || X.value < Q) Q = X.value;
    }
    return Q;
  }
};
P6.create = (Q) => {
  return new P6({ checks: [], typeName: j.ZodBigInt, coerce: Q?.coerce ?? false, ...l(Q) });
};
var O9 = class extends d {
  _parse(Q) {
    if (this._def.coerce) Q.data = Boolean(Q.data);
    if (this._getType(Q) !== I.boolean) {
      let Y = this._getOrReturnCtx(Q);
      return b(Y, { code: M.invalid_type, expected: I.boolean, received: Y.parsedType }), g;
    }
    return C0(Q.data);
  }
};
O9.create = (Q) => {
  return new O9({ typeName: j.ZodBoolean, coerce: Q?.coerce || false, ...l(Q) });
};
var G4 = class _G4 extends d {
  _parse(Q) {
    if (this._def.coerce) Q.data = new Date(Q.data);
    if (this._getType(Q) !== I.date) {
      let W = this._getOrReturnCtx(Q);
      return b(W, { code: M.invalid_type, expected: I.date, received: W.parsedType }), g;
    }
    if (Number.isNaN(Q.data.getTime())) {
      let W = this._getOrReturnCtx(Q);
      return b(W, { code: M.invalid_date }), g;
    }
    let Y = new b0(), $ = void 0;
    for (let W of this._def.checks) if (W.kind === "min") {
      if (Q.data.getTime() < W.value) $ = this._getOrReturnCtx(Q, $), b($, { code: M.too_small, message: W.message, inclusive: true, exact: false, minimum: W.value, type: "date" }), Y.dirty();
    } else if (W.kind === "max") {
      if (Q.data.getTime() > W.value) $ = this._getOrReturnCtx(Q, $), b($, { code: M.too_big, message: W.message, inclusive: true, exact: false, maximum: W.value, type: "date" }), Y.dirty();
    } else n.assertNever(W);
    return { status: Y.value, value: new Date(Q.data.getTime()) };
  }
  _addCheck(Q) {
    return new _G4({ ...this._def, checks: [...this._def.checks, Q] });
  }
  min(Q, X) {
    return this._addCheck({ kind: "min", value: Q.getTime(), message: Z.toString(X) });
  }
  max(Q, X) {
    return this._addCheck({ kind: "max", value: Q.getTime(), message: Z.toString(X) });
  }
  get minDate() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "min") {
      if (Q === null || X.value > Q) Q = X.value;
    }
    return Q != null ? new Date(Q) : null;
  }
  get maxDate() {
    let Q = null;
    for (let X of this._def.checks) if (X.kind === "max") {
      if (Q === null || X.value < Q) Q = X.value;
    }
    return Q != null ? new Date(Q) : null;
  }
};
G4.create = (Q) => {
  return new G4({ checks: [], coerce: Q?.coerce || false, typeName: j.ZodDate, ...l(Q) });
};
var w9 = class extends d {
  _parse(Q) {
    if (this._getType(Q) !== I.symbol) {
      let Y = this._getOrReturnCtx(Q);
      return b(Y, { code: M.invalid_type, expected: I.symbol, received: Y.parsedType }), g;
    }
    return C0(Q.data);
  }
};
w9.create = (Q) => {
  return new w9({ typeName: j.ZodSymbol, ...l(Q) });
};
var H4 = class extends d {
  _parse(Q) {
    if (this._getType(Q) !== I.undefined) {
      let Y = this._getOrReturnCtx(Q);
      return b(Y, { code: M.invalid_type, expected: I.undefined, received: Y.parsedType }), g;
    }
    return C0(Q.data);
  }
};
H4.create = (Q) => {
  return new H4({ typeName: j.ZodUndefined, ...l(Q) });
};
var B4 = class extends d {
  _parse(Q) {
    if (this._getType(Q) !== I.null) {
      let Y = this._getOrReturnCtx(Q);
      return b(Y, { code: M.invalid_type, expected: I.null, received: Y.parsedType }), g;
    }
    return C0(Q.data);
  }
};
B4.create = (Q) => {
  return new B4({ typeName: j.ZodNull, ...l(Q) });
};
var M9 = class extends d {
  constructor() {
    super(...arguments);
    this._any = true;
  }
  _parse(Q) {
    return C0(Q.data);
  }
};
M9.create = (Q) => {
  return new M9({ typeName: j.ZodAny, ...l(Q) });
};
var s1 = class extends d {
  constructor() {
    super(...arguments);
    this._unknown = true;
  }
  _parse(Q) {
    return C0(Q.data);
  }
};
s1.create = (Q) => {
  return new s1({ typeName: j.ZodUnknown, ...l(Q) });
};
var M1 = class extends d {
  _parse(Q) {
    let X = this._getOrReturnCtx(Q);
    return b(X, { code: M.invalid_type, expected: I.never, received: X.parsedType }), g;
  }
};
M1.create = (Q) => {
  return new M1({ typeName: j.ZodNever, ...l(Q) });
};
var A9 = class extends d {
  _parse(Q) {
    if (this._getType(Q) !== I.undefined) {
      let Y = this._getOrReturnCtx(Q);
      return b(Y, { code: M.invalid_type, expected: I.void, received: Y.parsedType }), g;
    }
    return C0(Q.data);
  }
};
A9.create = (Q) => {
  return new A9({ typeName: j.ZodVoid, ...l(Q) });
};
var W1 = class _W1 extends d {
  _parse(Q) {
    let { ctx: X, status: Y } = this._processInputParams(Q), $ = this._def;
    if (X.parsedType !== I.array) return b(X, { code: M.invalid_type, expected: I.array, received: X.parsedType }), g;
    if ($.exactLength !== null) {
      let J = X.data.length > $.exactLength.value, G = X.data.length < $.exactLength.value;
      if (J || G) b(X, { code: J ? M.too_big : M.too_small, minimum: G ? $.exactLength.value : void 0, maximum: J ? $.exactLength.value : void 0, type: "array", inclusive: true, exact: true, message: $.exactLength.message }), Y.dirty();
    }
    if ($.minLength !== null) {
      if (X.data.length < $.minLength.value) b(X, { code: M.too_small, minimum: $.minLength.value, type: "array", inclusive: true, exact: false, message: $.minLength.message }), Y.dirty();
    }
    if ($.maxLength !== null) {
      if (X.data.length > $.maxLength.value) b(X, { code: M.too_big, maximum: $.maxLength.value, type: "array", inclusive: true, exact: false, message: $.maxLength.message }), Y.dirty();
    }
    if (X.common.async) return Promise.all([...X.data].map((J, G) => {
      return $.type._parseAsync(new n0(X, J, X.path, G));
    })).then((J) => {
      return b0.mergeArray(Y, J);
    });
    let W = [...X.data].map((J, G) => {
      return $.type._parseSync(new n0(X, J, X.path, G));
    });
    return b0.mergeArray(Y, W);
  }
  get element() {
    return this._def.type;
  }
  min(Q, X) {
    return new _W1({ ...this._def, minLength: { value: Q, message: Z.toString(X) } });
  }
  max(Q, X) {
    return new _W1({ ...this._def, maxLength: { value: Q, message: Z.toString(X) } });
  }
  length(Q, X) {
    return new _W1({ ...this._def, exactLength: { value: Q, message: Z.toString(X) } });
  }
  nonempty(Q) {
    return this.min(1, Q);
  }
};
W1.create = (Q, X) => {
  return new W1({ type: Q, minLength: null, maxLength: null, exactLength: null, typeName: j.ZodArray, ...l(X) });
};
function E6(Q) {
  if (Q instanceof q0) {
    let X = {};
    for (let Y in Q.shape) {
      let $ = Q.shape[Y];
      X[Y] = J1.create(E6($));
    }
    return new q0({ ...Q._def, shape: () => X });
  } else if (Q instanceof W1) return new W1({ ...Q._def, type: E6(Q.element) });
  else if (Q instanceof J1) return J1.create(E6(Q.unwrap()));
  else if (Q instanceof T1) return T1.create(E6(Q.unwrap()));
  else if (Q instanceof A1) return A1.create(Q.items.map((X) => E6(X)));
  else return Q;
}
var q0 = class _q0 extends d {
  constructor() {
    super(...arguments);
    this._cached = null, this.nonstrict = this.passthrough, this.augment = this.extend;
  }
  _getCached() {
    if (this._cached !== null) return this._cached;
    let Q = this._def.shape(), X = n.objectKeys(Q);
    return this._cached = { shape: Q, keys: X }, this._cached;
  }
  _parse(Q) {
    if (this._getType(Q) !== I.object) {
      let B = this._getOrReturnCtx(Q);
      return b(B, { code: M.invalid_type, expected: I.object, received: B.parsedType }), g;
    }
    let { status: Y, ctx: $ } = this._processInputParams(Q), { shape: W, keys: J } = this._getCached(), G = [];
    if (!(this._def.catchall instanceof M1 && this._def.unknownKeys === "strip")) {
      for (let B in $.data) if (!J.includes(B)) G.push(B);
    }
    let H = [];
    for (let B of J) {
      let z = W[B], K = $.data[B];
      H.push({ key: { status: "valid", value: B }, value: z._parse(new n0($, K, $.path, B)), alwaysSet: B in $.data });
    }
    if (this._def.catchall instanceof M1) {
      let B = this._def.unknownKeys;
      if (B === "passthrough") for (let z of G) H.push({ key: { status: "valid", value: z }, value: { status: "valid", value: $.data[z] } });
      else if (B === "strict") {
        if (G.length > 0) b($, { code: M.unrecognized_keys, keys: G }), Y.dirty();
      } else if (B === "strip") ;
      else throw Error("Internal ZodObject error: invalid unknownKeys value.");
    } else {
      let B = this._def.catchall;
      for (let z of G) {
        let K = $.data[z];
        H.push({ key: { status: "valid", value: z }, value: B._parse(new n0($, K, $.path, z)), alwaysSet: z in $.data });
      }
    }
    if ($.common.async) return Promise.resolve().then(async () => {
      let B = [];
      for (let z of H) {
        let K = await z.key, q = await z.value;
        B.push({ key: K, value: q, alwaysSet: z.alwaysSet });
      }
      return B;
    }).then((B) => {
      return b0.mergeObjectSync(Y, B);
    });
    else return b0.mergeObjectSync(Y, H);
  }
  get shape() {
    return this._def.shape();
  }
  strict(Q) {
    return Z.errToObj, new _q0({ ...this._def, unknownKeys: "strict", ...Q !== void 0 ? { errorMap: (X, Y) => {
      let $ = this._def.errorMap?.(X, Y).message ?? Y.defaultError;
      if (X.code === "unrecognized_keys") return { message: Z.errToObj(Q).message ?? $ };
      return { message: $ };
    } } : {} });
  }
  strip() {
    return new _q0({ ...this._def, unknownKeys: "strip" });
  }
  passthrough() {
    return new _q0({ ...this._def, unknownKeys: "passthrough" });
  }
  extend(Q) {
    return new _q0({ ...this._def, shape: () => ({ ...this._def.shape(), ...Q }) });
  }
  merge(Q) {
    return new _q0({ unknownKeys: Q._def.unknownKeys, catchall: Q._def.catchall, shape: () => ({ ...this._def.shape(), ...Q._def.shape() }), typeName: j.ZodObject });
  }
  setKey(Q, X) {
    return this.augment({ [Q]: X });
  }
  catchall(Q) {
    return new _q0({ ...this._def, catchall: Q });
  }
  pick(Q) {
    let X = {};
    for (let Y of n.objectKeys(Q)) if (Q[Y] && this.shape[Y]) X[Y] = this.shape[Y];
    return new _q0({ ...this._def, shape: () => X });
  }
  omit(Q) {
    let X = {};
    for (let Y of n.objectKeys(this.shape)) if (!Q[Y]) X[Y] = this.shape[Y];
    return new _q0({ ...this._def, shape: () => X });
  }
  deepPartial() {
    return E6(this);
  }
  partial(Q) {
    let X = {};
    for (let Y of n.objectKeys(this.shape)) {
      let $ = this.shape[Y];
      if (Q && !Q[Y]) X[Y] = $;
      else X[Y] = $.optional();
    }
    return new _q0({ ...this._def, shape: () => X });
  }
  required(Q) {
    let X = {};
    for (let Y of n.objectKeys(this.shape)) if (Q && !Q[Y]) X[Y] = this.shape[Y];
    else {
      let W = this.shape[Y];
      while (W instanceof J1) W = W._def.innerType;
      X[Y] = W;
    }
    return new _q0({ ...this._def, shape: () => X });
  }
  keyof() {
    return FW(n.objectKeys(this.shape));
  }
};
q0.create = (Q, X) => {
  return new q0({ shape: () => Q, unknownKeys: "strip", catchall: M1.create(), typeName: j.ZodObject, ...l(X) });
};
q0.strictCreate = (Q, X) => {
  return new q0({ shape: () => Q, unknownKeys: "strict", catchall: M1.create(), typeName: j.ZodObject, ...l(X) });
};
q0.lazycreate = (Q, X) => {
  return new q0({ shape: Q, unknownKeys: "strip", catchall: M1.create(), typeName: j.ZodObject, ...l(X) });
};
var z4 = class extends d {
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q), Y = this._def.options;
    function $(W) {
      for (let G of W) if (G.result.status === "valid") return G.result;
      for (let G of W) if (G.result.status === "dirty") return X.common.issues.push(...G.ctx.common.issues), G.result;
      let J = W.map((G) => new f0(G.ctx.common.issues));
      return b(X, { code: M.invalid_union, unionErrors: J }), g;
    }
    if (X.common.async) return Promise.all(Y.map(async (W) => {
      let J = { ...X, common: { ...X.common, issues: [] }, parent: null };
      return { result: await W._parseAsync({ data: X.data, path: X.path, parent: J }), ctx: J };
    })).then($);
    else {
      let W = void 0, J = [];
      for (let H of Y) {
        let B = { ...X, common: { ...X.common, issues: [] }, parent: null }, z = H._parseSync({ data: X.data, path: X.path, parent: B });
        if (z.status === "valid") return z;
        else if (z.status === "dirty" && !W) W = { result: z, ctx: B };
        if (B.common.issues.length) J.push(B.common.issues);
      }
      if (W) return X.common.issues.push(...W.ctx.common.issues), W.result;
      let G = J.map((H) => new f0(H));
      return b(X, { code: M.invalid_union, unionErrors: G }), g;
    }
  }
  get options() {
    return this._def.options;
  }
};
z4.create = (Q, X) => {
  return new z4({ options: Q, typeName: j.ZodUnion, ...l(X) });
};
var O1 = (Q) => {
  if (Q instanceof V4) return O1(Q.schema);
  else if (Q instanceof G1) return O1(Q.innerType());
  else if (Q instanceof q4) return [Q.value];
  else if (Q instanceof e1) return Q.options;
  else if (Q instanceof U4) return n.objectValues(Q.enum);
  else if (Q instanceof L4) return O1(Q._def.innerType);
  else if (Q instanceof H4) return [void 0];
  else if (Q instanceof B4) return [null];
  else if (Q instanceof J1) return [void 0, ...O1(Q.unwrap())];
  else if (Q instanceof T1) return [null, ...O1(Q.unwrap())];
  else if (Q instanceof MQ) return O1(Q.unwrap());
  else if (Q instanceof N4) return O1(Q.unwrap());
  else if (Q instanceof F4) return O1(Q._def.innerType);
  else return [];
};
var wQ = class _wQ extends d {
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q);
    if (X.parsedType !== I.object) return b(X, { code: M.invalid_type, expected: I.object, received: X.parsedType }), g;
    let Y = this.discriminator, $ = X.data[Y], W = this.optionsMap.get($);
    if (!W) return b(X, { code: M.invalid_union_discriminator, options: Array.from(this.optionsMap.keys()), path: [Y] }), g;
    if (X.common.async) return W._parseAsync({ data: X.data, path: X.path, parent: X });
    else return W._parseSync({ data: X.data, path: X.path, parent: X });
  }
  get discriminator() {
    return this._def.discriminator;
  }
  get options() {
    return this._def.options;
  }
  get optionsMap() {
    return this._def.optionsMap;
  }
  static create(Q, X, Y) {
    let $ = /* @__PURE__ */ new Map();
    for (let W of X) {
      let J = O1(W.shape[Q]);
      if (!J.length) throw Error(`A discriminator value for key \`${Q}\` could not be extracted from all schema options`);
      for (let G of J) {
        if ($.has(G)) throw Error(`Discriminator property ${String(Q)} has duplicate value ${String(G)}`);
        $.set(G, W);
      }
    }
    return new _wQ({ typeName: j.ZodDiscriminatedUnion, discriminator: Q, options: X, optionsMap: $, ...l(Y) });
  }
};
function OQ(Q, X) {
  let Y = D1(Q), $ = D1(X);
  if (Q === X) return { valid: true, data: Q };
  else if (Y === I.object && $ === I.object) {
    let W = n.objectKeys(X), J = n.objectKeys(Q).filter((H) => W.indexOf(H) !== -1), G = { ...Q, ...X };
    for (let H of J) {
      let B = OQ(Q[H], X[H]);
      if (!B.valid) return { valid: false };
      G[H] = B.data;
    }
    return { valid: true, data: G };
  } else if (Y === I.array && $ === I.array) {
    if (Q.length !== X.length) return { valid: false };
    let W = [];
    for (let J = 0; J < Q.length; J++) {
      let G = Q[J], H = X[J], B = OQ(G, H);
      if (!B.valid) return { valid: false };
      W.push(B.data);
    }
    return { valid: true, data: W };
  } else if (Y === I.date && $ === I.date && +Q === +X) return { valid: true, data: Q };
  else return { valid: false };
}
var K4 = class extends d {
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q), $ = (W, J) => {
      if (FQ(W) || FQ(J)) return g;
      let G = OQ(W.value, J.value);
      if (!G.valid) return b(Y, { code: M.invalid_intersection_types }), g;
      if (NQ(W) || NQ(J)) X.dirty();
      return { status: X.value, value: G.data };
    };
    if (Y.common.async) return Promise.all([this._def.left._parseAsync({ data: Y.data, path: Y.path, parent: Y }), this._def.right._parseAsync({ data: Y.data, path: Y.path, parent: Y })]).then(([W, J]) => $(W, J));
    else return $(this._def.left._parseSync({ data: Y.data, path: Y.path, parent: Y }), this._def.right._parseSync({ data: Y.data, path: Y.path, parent: Y }));
  }
};
K4.create = (Q, X, Y) => {
  return new K4({ left: Q, right: X, typeName: j.ZodIntersection, ...l(Y) });
};
var A1 = class _A1 extends d {
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q);
    if (Y.parsedType !== I.array) return b(Y, { code: M.invalid_type, expected: I.array, received: Y.parsedType }), g;
    if (Y.data.length < this._def.items.length) return b(Y, { code: M.too_small, minimum: this._def.items.length, inclusive: true, exact: false, type: "array" }), g;
    if (!this._def.rest && Y.data.length > this._def.items.length) b(Y, { code: M.too_big, maximum: this._def.items.length, inclusive: true, exact: false, type: "array" }), X.dirty();
    let W = [...Y.data].map((J, G) => {
      let H = this._def.items[G] || this._def.rest;
      if (!H) return null;
      return H._parse(new n0(Y, J, Y.path, G));
    }).filter((J) => !!J);
    if (Y.common.async) return Promise.all(W).then((J) => {
      return b0.mergeArray(X, J);
    });
    else return b0.mergeArray(X, W);
  }
  get items() {
    return this._def.items;
  }
  rest(Q) {
    return new _A1({ ...this._def, rest: Q });
  }
};
A1.create = (Q, X) => {
  if (!Array.isArray(Q)) throw Error("You must pass an array of schemas to z.tuple([ ... ])");
  return new A1({ items: Q, typeName: j.ZodTuple, rest: null, ...l(X) });
};
var j9 = class _j9 extends d {
  get keySchema() {
    return this._def.keyType;
  }
  get valueSchema() {
    return this._def.valueType;
  }
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q);
    if (Y.parsedType !== I.object) return b(Y, { code: M.invalid_type, expected: I.object, received: Y.parsedType }), g;
    let $ = [], W = this._def.keyType, J = this._def.valueType;
    for (let G in Y.data) $.push({ key: W._parse(new n0(Y, G, Y.path, G)), value: J._parse(new n0(Y, Y.data[G], Y.path, G)), alwaysSet: G in Y.data });
    if (Y.common.async) return b0.mergeObjectAsync(X, $);
    else return b0.mergeObjectSync(X, $);
  }
  get element() {
    return this._def.valueType;
  }
  static create(Q, X, Y) {
    if (X instanceof d) return new _j9({ keyType: Q, valueType: X, typeName: j.ZodRecord, ...l(Y) });
    return new _j9({ keyType: w1.create(), valueType: Q, typeName: j.ZodRecord, ...l(X) });
  }
};
var R9 = class extends d {
  get keySchema() {
    return this._def.keyType;
  }
  get valueSchema() {
    return this._def.valueType;
  }
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q);
    if (Y.parsedType !== I.map) return b(Y, { code: M.invalid_type, expected: I.map, received: Y.parsedType }), g;
    let $ = this._def.keyType, W = this._def.valueType, J = [...Y.data.entries()].map(([G, H], B) => {
      return { key: $._parse(new n0(Y, G, Y.path, [B, "key"])), value: W._parse(new n0(Y, H, Y.path, [B, "value"])) };
    });
    if (Y.common.async) {
      let G = /* @__PURE__ */ new Map();
      return Promise.resolve().then(async () => {
        for (let H of J) {
          let B = await H.key, z = await H.value;
          if (B.status === "aborted" || z.status === "aborted") return g;
          if (B.status === "dirty" || z.status === "dirty") X.dirty();
          G.set(B.value, z.value);
        }
        return { status: X.value, value: G };
      });
    } else {
      let G = /* @__PURE__ */ new Map();
      for (let H of J) {
        let { key: B, value: z } = H;
        if (B.status === "aborted" || z.status === "aborted") return g;
        if (B.status === "dirty" || z.status === "dirty") X.dirty();
        G.set(B.value, z.value);
      }
      return { status: X.value, value: G };
    }
  }
};
R9.create = (Q, X, Y) => {
  return new R9({ valueType: X, keyType: Q, typeName: j.ZodMap, ...l(Y) });
};
var S6 = class _S6 extends d {
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q);
    if (Y.parsedType !== I.set) return b(Y, { code: M.invalid_type, expected: I.set, received: Y.parsedType }), g;
    let $ = this._def;
    if ($.minSize !== null) {
      if (Y.data.size < $.minSize.value) b(Y, { code: M.too_small, minimum: $.minSize.value, type: "set", inclusive: true, exact: false, message: $.minSize.message }), X.dirty();
    }
    if ($.maxSize !== null) {
      if (Y.data.size > $.maxSize.value) b(Y, { code: M.too_big, maximum: $.maxSize.value, type: "set", inclusive: true, exact: false, message: $.maxSize.message }), X.dirty();
    }
    let W = this._def.valueType;
    function J(H) {
      let B = /* @__PURE__ */ new Set();
      for (let z of H) {
        if (z.status === "aborted") return g;
        if (z.status === "dirty") X.dirty();
        B.add(z.value);
      }
      return { status: X.value, value: B };
    }
    let G = [...Y.data.values()].map((H, B) => W._parse(new n0(Y, H, Y.path, B)));
    if (Y.common.async) return Promise.all(G).then((H) => J(H));
    else return J(G);
  }
  min(Q, X) {
    return new _S6({ ...this._def, minSize: { value: Q, message: Z.toString(X) } });
  }
  max(Q, X) {
    return new _S6({ ...this._def, maxSize: { value: Q, message: Z.toString(X) } });
  }
  size(Q, X) {
    return this.min(Q, X).max(Q, X);
  }
  nonempty(Q) {
    return this.min(1, Q);
  }
};
S6.create = (Q, X) => {
  return new S6({ valueType: Q, minSize: null, maxSize: null, typeName: j.ZodSet, ...l(X) });
};
var J4 = class _J4 extends d {
  constructor() {
    super(...arguments);
    this.validate = this.implement;
  }
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q);
    if (X.parsedType !== I.function) return b(X, { code: M.invalid_type, expected: I.function, received: X.parsedType }), g;
    function Y(G, H) {
      return D9({ data: G, path: X.path, errorMaps: [X.common.contextualErrorMap, X.schemaErrorMap, $4(), _1].filter((B) => !!B), issueData: { code: M.invalid_arguments, argumentsError: H } });
    }
    function $(G, H) {
      return D9({ data: G, path: X.path, errorMaps: [X.common.contextualErrorMap, X.schemaErrorMap, $4(), _1].filter((B) => !!B), issueData: { code: M.invalid_return_type, returnTypeError: H } });
    }
    let W = { errorMap: X.common.contextualErrorMap }, J = X.data;
    if (this._def.returns instanceof Z6) {
      let G = this;
      return C0(async function(...H) {
        let B = new f0([]), z = await G._def.args.parseAsync(H, W).catch((U) => {
          throw B.addIssue(Y(H, U)), B;
        }), K = await Reflect.apply(J, this, z);
        return await G._def.returns._def.type.parseAsync(K, W).catch((U) => {
          throw B.addIssue($(K, U)), B;
        });
      });
    } else {
      let G = this;
      return C0(function(...H) {
        let B = G._def.args.safeParse(H, W);
        if (!B.success) throw new f0([Y(H, B.error)]);
        let z = Reflect.apply(J, this, B.data), K = G._def.returns.safeParse(z, W);
        if (!K.success) throw new f0([$(z, K.error)]);
        return K.data;
      });
    }
  }
  parameters() {
    return this._def.args;
  }
  returnType() {
    return this._def.returns;
  }
  args(...Q) {
    return new _J4({ ...this._def, args: A1.create(Q).rest(s1.create()) });
  }
  returns(Q) {
    return new _J4({ ...this._def, returns: Q });
  }
  implement(Q) {
    return this.parse(Q);
  }
  strictImplement(Q) {
    return this.parse(Q);
  }
  static create(Q, X, Y) {
    return new _J4({ args: Q ? Q : A1.create([]).rest(s1.create()), returns: X || s1.create(), typeName: j.ZodFunction, ...l(Y) });
  }
};
var V4 = class extends d {
  get schema() {
    return this._def.getter();
  }
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q);
    return this._def.getter()._parse({ data: X.data, path: X.path, parent: X });
  }
};
V4.create = (Q, X) => {
  return new V4({ getter: Q, typeName: j.ZodLazy, ...l(X) });
};
var q4 = class extends d {
  _parse(Q) {
    if (Q.data !== this._def.value) {
      let X = this._getOrReturnCtx(Q);
      return b(X, { received: X.data, code: M.invalid_literal, expected: this._def.value }), g;
    }
    return { status: "valid", value: Q.data };
  }
  get value() {
    return this._def.value;
  }
};
q4.create = (Q, X) => {
  return new q4({ value: Q, typeName: j.ZodLiteral, ...l(X) });
};
function FW(Q, X) {
  return new e1({ values: Q, typeName: j.ZodEnum, ...l(X) });
}
var e1 = class _e1 extends d {
  _parse(Q) {
    if (typeof Q.data !== "string") {
      let X = this._getOrReturnCtx(Q), Y = this._def.values;
      return b(X, { expected: n.joinValues(Y), received: X.parsedType, code: M.invalid_type }), g;
    }
    if (!this._cache) this._cache = new Set(this._def.values);
    if (!this._cache.has(Q.data)) {
      let X = this._getOrReturnCtx(Q), Y = this._def.values;
      return b(X, { received: X.data, code: M.invalid_enum_value, options: Y }), g;
    }
    return C0(Q.data);
  }
  get options() {
    return this._def.values;
  }
  get enum() {
    let Q = {};
    for (let X of this._def.values) Q[X] = X;
    return Q;
  }
  get Values() {
    let Q = {};
    for (let X of this._def.values) Q[X] = X;
    return Q;
  }
  get Enum() {
    let Q = {};
    for (let X of this._def.values) Q[X] = X;
    return Q;
  }
  extract(Q, X = this._def) {
    return _e1.create(Q, { ...this._def, ...X });
  }
  exclude(Q, X = this._def) {
    return _e1.create(this.options.filter((Y) => !Q.includes(Y)), { ...this._def, ...X });
  }
};
e1.create = FW;
var U4 = class extends d {
  _parse(Q) {
    let X = n.getValidEnumValues(this._def.values), Y = this._getOrReturnCtx(Q);
    if (Y.parsedType !== I.string && Y.parsedType !== I.number) {
      let $ = n.objectValues(X);
      return b(Y, { expected: n.joinValues($), received: Y.parsedType, code: M.invalid_type }), g;
    }
    if (!this._cache) this._cache = new Set(n.getValidEnumValues(this._def.values));
    if (!this._cache.has(Q.data)) {
      let $ = n.objectValues(X);
      return b(Y, { received: Y.data, code: M.invalid_enum_value, options: $ }), g;
    }
    return C0(Q.data);
  }
  get enum() {
    return this._def.values;
  }
};
U4.create = (Q, X) => {
  return new U4({ values: Q, typeName: j.ZodNativeEnum, ...l(X) });
};
var Z6 = class extends d {
  unwrap() {
    return this._def.type;
  }
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q);
    if (X.parsedType !== I.promise && X.common.async === false) return b(X, { code: M.invalid_type, expected: I.promise, received: X.parsedType }), g;
    let Y = X.parsedType === I.promise ? X.data : Promise.resolve(X.data);
    return C0(Y.then(($) => {
      return this._def.type.parseAsync($, { path: X.path, errorMap: X.common.contextualErrorMap });
    }));
  }
};
Z6.create = (Q, X) => {
  return new Z6({ type: Q, typeName: j.ZodPromise, ...l(X) });
};
var G1 = class extends d {
  innerType() {
    return this._def.schema;
  }
  sourceType() {
    return this._def.schema._def.typeName === j.ZodEffects ? this._def.schema.sourceType() : this._def.schema;
  }
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q), $ = this._def.effect || null, W = { addIssue: (J) => {
      if (b(Y, J), J.fatal) X.abort();
      else X.dirty();
    }, get path() {
      return Y.path;
    } };
    if (W.addIssue = W.addIssue.bind(W), $.type === "preprocess") {
      let J = $.transform(Y.data, W);
      if (Y.common.async) return Promise.resolve(J).then(async (G) => {
        if (X.value === "aborted") return g;
        let H = await this._def.schema._parseAsync({ data: G, path: Y.path, parent: Y });
        if (H.status === "aborted") return g;
        if (H.status === "dirty") return I6(H.value);
        if (X.value === "dirty") return I6(H.value);
        return H;
      });
      else {
        if (X.value === "aborted") return g;
        let G = this._def.schema._parseSync({ data: J, path: Y.path, parent: Y });
        if (G.status === "aborted") return g;
        if (G.status === "dirty") return I6(G.value);
        if (X.value === "dirty") return I6(G.value);
        return G;
      }
    }
    if ($.type === "refinement") {
      let J = (G) => {
        let H = $.refinement(G, W);
        if (Y.common.async) return Promise.resolve(H);
        if (H instanceof Promise) throw Error("Async refinement encountered during synchronous parse operation. Use .parseAsync instead.");
        return G;
      };
      if (Y.common.async === false) {
        let G = this._def.schema._parseSync({ data: Y.data, path: Y.path, parent: Y });
        if (G.status === "aborted") return g;
        if (G.status === "dirty") X.dirty();
        return J(G.value), { status: X.value, value: G.value };
      } else return this._def.schema._parseAsync({ data: Y.data, path: Y.path, parent: Y }).then((G) => {
        if (G.status === "aborted") return g;
        if (G.status === "dirty") X.dirty();
        return J(G.value).then(() => {
          return { status: X.value, value: G.value };
        });
      });
    }
    if ($.type === "transform") if (Y.common.async === false) {
      let J = this._def.schema._parseSync({ data: Y.data, path: Y.path, parent: Y });
      if (!a1(J)) return g;
      let G = $.transform(J.value, W);
      if (G instanceof Promise) throw Error("Asynchronous transform encountered during synchronous parse operation. Use .parseAsync instead.");
      return { status: X.value, value: G };
    } else return this._def.schema._parseAsync({ data: Y.data, path: Y.path, parent: Y }).then((J) => {
      if (!a1(J)) return g;
      return Promise.resolve($.transform(J.value, W)).then((G) => ({ status: X.value, value: G }));
    });
    n.assertNever($);
  }
};
G1.create = (Q, X, Y) => {
  return new G1({ schema: Q, typeName: j.ZodEffects, effect: X, ...l(Y) });
};
G1.createWithPreprocess = (Q, X, Y) => {
  return new G1({ schema: X, effect: { type: "preprocess", transform: Q }, typeName: j.ZodEffects, ...l(Y) });
};
var J1 = class extends d {
  _parse(Q) {
    if (this._getType(Q) === I.undefined) return C0(void 0);
    return this._def.innerType._parse(Q);
  }
  unwrap() {
    return this._def.innerType;
  }
};
J1.create = (Q, X) => {
  return new J1({ innerType: Q, typeName: j.ZodOptional, ...l(X) });
};
var T1 = class extends d {
  _parse(Q) {
    if (this._getType(Q) === I.null) return C0(null);
    return this._def.innerType._parse(Q);
  }
  unwrap() {
    return this._def.innerType;
  }
};
T1.create = (Q, X) => {
  return new T1({ innerType: Q, typeName: j.ZodNullable, ...l(X) });
};
var L4 = class extends d {
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q), Y = X.data;
    if (X.parsedType === I.undefined) Y = this._def.defaultValue();
    return this._def.innerType._parse({ data: Y, path: X.path, parent: X });
  }
  removeDefault() {
    return this._def.innerType;
  }
};
L4.create = (Q, X) => {
  return new L4({ innerType: Q, typeName: j.ZodDefault, defaultValue: typeof X.default === "function" ? X.default : () => X.default, ...l(X) });
};
var F4 = class extends d {
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q), Y = { ...X, common: { ...X.common, issues: [] } }, $ = this._def.innerType._parse({ data: Y.data, path: Y.path, parent: { ...Y } });
    if (W4($)) return $.then((W) => {
      return { status: "valid", value: W.status === "valid" ? W.value : this._def.catchValue({ get error() {
        return new f0(Y.common.issues);
      }, input: Y.data }) };
    });
    else return { status: "valid", value: $.status === "valid" ? $.value : this._def.catchValue({ get error() {
      return new f0(Y.common.issues);
    }, input: Y.data }) };
  }
  removeCatch() {
    return this._def.innerType;
  }
};
F4.create = (Q, X) => {
  return new F4({ innerType: Q, typeName: j.ZodCatch, catchValue: typeof X.catch === "function" ? X.catch : () => X.catch, ...l(X) });
};
var I9 = class extends d {
  _parse(Q) {
    if (this._getType(Q) !== I.nan) {
      let Y = this._getOrReturnCtx(Q);
      return b(Y, { code: M.invalid_type, expected: I.nan, received: Y.parsedType }), g;
    }
    return { status: "valid", value: Q.data };
  }
};
I9.create = (Q) => {
  return new I9({ typeName: j.ZodNaN, ...l(Q) });
};
var jP = Symbol("zod_brand");
var MQ = class extends d {
  _parse(Q) {
    let { ctx: X } = this._processInputParams(Q), Y = X.data;
    return this._def.type._parse({ data: Y, path: X.path, parent: X });
  }
  unwrap() {
    return this._def.type;
  }
};
var E9 = class _E9 extends d {
  _parse(Q) {
    let { status: X, ctx: Y } = this._processInputParams(Q);
    if (Y.common.async) return (async () => {
      let W = await this._def.in._parseAsync({ data: Y.data, path: Y.path, parent: Y });
      if (W.status === "aborted") return g;
      if (W.status === "dirty") return X.dirty(), I6(W.value);
      else return this._def.out._parseAsync({ data: W.value, path: Y.path, parent: Y });
    })();
    else {
      let $ = this._def.in._parseSync({ data: Y.data, path: Y.path, parent: Y });
      if ($.status === "aborted") return g;
      if ($.status === "dirty") return X.dirty(), { status: "dirty", value: $.value };
      else return this._def.out._parseSync({ data: $.value, path: Y.path, parent: Y });
    }
  }
  static create(Q, X) {
    return new _E9({ in: Q, out: X, typeName: j.ZodPipeline });
  }
};
var N4 = class extends d {
  _parse(Q) {
    let X = this._def.innerType._parse(Q), Y = ($) => {
      if (a1($)) $.value = Object.freeze($.value);
      return $;
    };
    return W4(X) ? X.then(($) => Y($)) : Y(X);
  }
  unwrap() {
    return this._def.innerType;
  }
};
N4.create = (Q, X) => {
  return new N4({ innerType: Q, typeName: j.ZodReadonly, ...l(X) });
};
var RP = { object: q0.lazycreate };
var j;
(function(Q) {
  Q.ZodString = "ZodString", Q.ZodNumber = "ZodNumber", Q.ZodNaN = "ZodNaN", Q.ZodBigInt = "ZodBigInt", Q.ZodBoolean = "ZodBoolean", Q.ZodDate = "ZodDate", Q.ZodSymbol = "ZodSymbol", Q.ZodUndefined = "ZodUndefined", Q.ZodNull = "ZodNull", Q.ZodAny = "ZodAny", Q.ZodUnknown = "ZodUnknown", Q.ZodNever = "ZodNever", Q.ZodVoid = "ZodVoid", Q.ZodArray = "ZodArray", Q.ZodObject = "ZodObject", Q.ZodUnion = "ZodUnion", Q.ZodDiscriminatedUnion = "ZodDiscriminatedUnion", Q.ZodIntersection = "ZodIntersection", Q.ZodTuple = "ZodTuple", Q.ZodRecord = "ZodRecord", Q.ZodMap = "ZodMap", Q.ZodSet = "ZodSet", Q.ZodFunction = "ZodFunction", Q.ZodLazy = "ZodLazy", Q.ZodLiteral = "ZodLiteral", Q.ZodEnum = "ZodEnum", Q.ZodEffects = "ZodEffects", Q.ZodNativeEnum = "ZodNativeEnum", Q.ZodOptional = "ZodOptional", Q.ZodNullable = "ZodNullable", Q.ZodDefault = "ZodDefault", Q.ZodCatch = "ZodCatch", Q.ZodPromise = "ZodPromise", Q.ZodBranded = "ZodBranded", Q.ZodPipeline = "ZodPipeline", Q.ZodReadonly = "ZodReadonly";
})(j || (j = {}));
var IP = w1.create;
var EP = b6.create;
var bP = I9.create;
var PP = P6.create;
var SP = O9.create;
var ZP = G4.create;
var CP = w9.create;
var kP = H4.create;
var vP = B4.create;
var _P = M9.create;
var TP = s1.create;
var xP = M1.create;
var yP = A9.create;
var gP = W1.create;
var NW = q0.create;
var hP = q0.strictCreate;
var fP = z4.create;
var uP = wQ.create;
var lP = K4.create;
var mP = A1.create;
var cP = j9.create;
var pP = R9.create;
var dP = S6.create;
var iP = J4.create;
var nP = V4.create;
var rP = q4.create;
var oP = e1.create;
var tP = U4.create;
var aP = Z6.create;
var sP = G1.create;
var eP = J1.create;
var QS = T1.create;
var XS = G1.createWithPreprocess;
var YS = E9.create;
var _q = Object.freeze({ status: "aborted" });
function D(Q, X, Y) {
  function $(H, B) {
    var z;
    Object.defineProperty(H, "_zod", { value: H._zod ?? {}, enumerable: false }), (z = H._zod).traits ?? (z.traits = /* @__PURE__ */ new Set()), H._zod.traits.add(Q), X(H, B);
    for (let K in G.prototype) if (!(K in H)) Object.defineProperty(H, K, { value: G.prototype[K].bind(H) });
    H._zod.constr = G, H._zod.def = B;
  }
  let W = Y?.Parent ?? Object;
  class J extends W {
  }
  Object.defineProperty(J, "name", { value: Q });
  function G(H) {
    var B;
    let z = Y?.Parent ? new J() : this;
    $(z, H), (B = z._zod).deferred ?? (B.deferred = []);
    for (let K of z._zod.deferred) K();
    return z;
  }
  return Object.defineProperty(G, "init", { value: $ }), Object.defineProperty(G, Symbol.hasInstance, { value: (H) => {
    if (Y?.Parent && H instanceof Y.Parent) return true;
    return H?._zod?.traits?.has(Q);
  } }), Object.defineProperty(G, "name", { value: Q }), G;
}
var Tq = Symbol("zod_brand");
var x1 = class extends Error {
  constructor() {
    super("Encountered Promise during synchronous parse. Use .parseAsync() instead.");
  }
};
var b9 = {};
function u0(Q) {
  if (Q) Object.assign(b9, Q);
  return b9;
}
var i = {};
U7(i, { unwrapMessage: () => D4, stringifyPrimitive: () => Z9, required: () => aq, randomString: () => mq, propertyKeyTypes: () => bQ, promiseAllObject: () => lq, primitiveTypes: () => DW, prefixIssues: () => H1, pick: () => iq, partial: () => tq, optionalKeys: () => PQ, omit: () => nq, numKeys: () => cq, nullish: () => M4, normalizeParams: () => y, merge: () => oq, jsonStringifyReplacer: () => jQ, joinValues: () => P9, issue: () => ZQ, isPlainObject: () => k6, isObject: () => C6, getSizableOrigin: () => wW, getParsedType: () => pq, getLengthableOrigin: () => j4, getEnumValues: () => O4, getElementAtPath: () => uq, floatSafeRemainder: () => RQ, finalizeIssue: () => r0, extend: () => rq, escapeRegex: () => y1, esc: () => Q6, defineLazy: () => $0, createTransparentProxy: () => dq, clone: () => l0, cleanRegex: () => A4, cleanEnum: () => sq, captureStackTrace: () => S9, cached: () => w4, assignProp: () => IQ, assertNotEqual: () => yq, assertNever: () => hq, assertIs: () => gq, assertEqual: () => xq, assert: () => fq, allowsEval: () => EQ, aborted: () => X6, NUMBER_FORMAT_RANGES: () => SQ, Class: () => MW, BIGINT_FORMAT_RANGES: () => OW });
function xq(Q) {
  return Q;
}
function yq(Q) {
  return Q;
}
function gq(Q) {
}
function hq(Q) {
  throw Error();
}
function fq(Q) {
}
function O4(Q) {
  let X = Object.values(Q).filter(($) => typeof $ === "number");
  return Object.entries(Q).filter(([$, W]) => X.indexOf(+$) === -1).map(([$, W]) => W);
}
function P9(Q, X = "|") {
  return Q.map((Y) => Z9(Y)).join(X);
}
function jQ(Q, X) {
  if (typeof X === "bigint") return X.toString();
  return X;
}
function w4(Q) {
  return { get value() {
    {
      let Y = Q();
      return Object.defineProperty(this, "value", { value: Y }), Y;
    }
    throw Error("cached value already set");
  } };
}
function M4(Q) {
  return Q === null || Q === void 0;
}
function A4(Q) {
  let X = Q.startsWith("^") ? 1 : 0, Y = Q.endsWith("$") ? Q.length - 1 : Q.length;
  return Q.slice(X, Y);
}
function RQ(Q, X) {
  let Y = (Q.toString().split(".")[1] || "").length, $ = (X.toString().split(".")[1] || "").length, W = Y > $ ? Y : $, J = Number.parseInt(Q.toFixed(W).replace(".", "")), G = Number.parseInt(X.toFixed(W).replace(".", ""));
  return J % G / 10 ** W;
}
function $0(Q, X, Y) {
  Object.defineProperty(Q, X, { get() {
    {
      let W = Y();
      return Q[X] = W, W;
    }
    throw Error("cached value already set");
  }, set(W) {
    Object.defineProperty(Q, X, { value: W });
  }, configurable: true });
}
function IQ(Q, X, Y) {
  Object.defineProperty(Q, X, { value: Y, writable: true, enumerable: true, configurable: true });
}
function uq(Q, X) {
  if (!X) return Q;
  return X.reduce((Y, $) => Y?.[$], Q);
}
function lq(Q) {
  let X = Object.keys(Q), Y = X.map(($) => Q[$]);
  return Promise.all(Y).then(($) => {
    let W = {};
    for (let J = 0; J < X.length; J++) W[X[J]] = $[J];
    return W;
  });
}
function mq(Q = 10) {
  let Y = "";
  for (let $ = 0; $ < Q; $++) Y += "abcdefghijklmnopqrstuvwxyz"[Math.floor(Math.random() * 26)];
  return Y;
}
function Q6(Q) {
  return JSON.stringify(Q);
}
var S9 = Error.captureStackTrace ? Error.captureStackTrace : (...Q) => {
};
function C6(Q) {
  return typeof Q === "object" && Q !== null && !Array.isArray(Q);
}
var EQ = w4(() => {
  if (typeof navigator < "u" && navigator?.userAgent?.includes("Cloudflare")) return false;
  try {
    return new Function(""), true;
  } catch (Q) {
    return false;
  }
});
function k6(Q) {
  if (C6(Q) === false) return false;
  let X = Q.constructor;
  if (X === void 0) return true;
  let Y = X.prototype;
  if (C6(Y) === false) return false;
  if (Object.prototype.hasOwnProperty.call(Y, "isPrototypeOf") === false) return false;
  return true;
}
function cq(Q) {
  let X = 0;
  for (let Y in Q) if (Object.prototype.hasOwnProperty.call(Q, Y)) X++;
  return X;
}
var pq = (Q) => {
  let X = typeof Q;
  switch (X) {
    case "undefined":
      return "undefined";
    case "string":
      return "string";
    case "number":
      return Number.isNaN(Q) ? "nan" : "number";
    case "boolean":
      return "boolean";
    case "function":
      return "function";
    case "bigint":
      return "bigint";
    case "symbol":
      return "symbol";
    case "object":
      if (Array.isArray(Q)) return "array";
      if (Q === null) return "null";
      if (Q.then && typeof Q.then === "function" && Q.catch && typeof Q.catch === "function") return "promise";
      if (typeof Map < "u" && Q instanceof Map) return "map";
      if (typeof Set < "u" && Q instanceof Set) return "set";
      if (typeof Date < "u" && Q instanceof Date) return "date";
      if (typeof File < "u" && Q instanceof File) return "file";
      return "object";
    default:
      throw Error(`Unknown data type: ${X}`);
  }
};
var bQ = /* @__PURE__ */ new Set(["string", "number", "symbol"]);
var DW = /* @__PURE__ */ new Set(["string", "number", "bigint", "boolean", "symbol", "undefined"]);
function y1(Q) {
  return Q.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
function l0(Q, X, Y) {
  let $ = new Q._zod.constr(X ?? Q._zod.def);
  if (!X || Y?.parent) $._zod.parent = Q;
  return $;
}
function y(Q) {
  let X = Q;
  if (!X) return {};
  if (typeof X === "string") return { error: () => X };
  if (X?.message !== void 0) {
    if (X?.error !== void 0) throw Error("Cannot specify both `message` and `error` params");
    X.error = X.message;
  }
  if (delete X.message, typeof X.error === "string") return { ...X, error: () => X.error };
  return X;
}
function dq(Q) {
  let X;
  return new Proxy({}, { get(Y, $, W) {
    return X ?? (X = Q()), Reflect.get(X, $, W);
  }, set(Y, $, W, J) {
    return X ?? (X = Q()), Reflect.set(X, $, W, J);
  }, has(Y, $) {
    return X ?? (X = Q()), Reflect.has(X, $);
  }, deleteProperty(Y, $) {
    return X ?? (X = Q()), Reflect.deleteProperty(X, $);
  }, ownKeys(Y) {
    return X ?? (X = Q()), Reflect.ownKeys(X);
  }, getOwnPropertyDescriptor(Y, $) {
    return X ?? (X = Q()), Reflect.getOwnPropertyDescriptor(X, $);
  }, defineProperty(Y, $, W) {
    return X ?? (X = Q()), Reflect.defineProperty(X, $, W);
  } });
}
function Z9(Q) {
  if (typeof Q === "bigint") return Q.toString() + "n";
  if (typeof Q === "string") return `"${Q}"`;
  return `${Q}`;
}
function PQ(Q) {
  return Object.keys(Q).filter((X) => {
    return Q[X]._zod.optin === "optional" && Q[X]._zod.optout === "optional";
  });
}
var SQ = { safeint: [Number.MIN_SAFE_INTEGER, Number.MAX_SAFE_INTEGER], int32: [-2147483648, 2147483647], uint32: [0, 4294967295], float32: [-34028234663852886e22, 34028234663852886e22], float64: [-Number.MAX_VALUE, Number.MAX_VALUE] };
var OW = { int64: [BigInt("-9223372036854775808"), BigInt("9223372036854775807")], uint64: [BigInt(0), BigInt("18446744073709551615")] };
function iq(Q, X) {
  let Y = {}, $ = Q._zod.def;
  for (let W in X) {
    if (!(W in $.shape)) throw Error(`Unrecognized key: "${W}"`);
    if (!X[W]) continue;
    Y[W] = $.shape[W];
  }
  return l0(Q, { ...Q._zod.def, shape: Y, checks: [] });
}
function nq(Q, X) {
  let Y = { ...Q._zod.def.shape }, $ = Q._zod.def;
  for (let W in X) {
    if (!(W in $.shape)) throw Error(`Unrecognized key: "${W}"`);
    if (!X[W]) continue;
    delete Y[W];
  }
  return l0(Q, { ...Q._zod.def, shape: Y, checks: [] });
}
function rq(Q, X) {
  if (!k6(X)) throw Error("Invalid input to extend: expected a plain object");
  let Y = { ...Q._zod.def, get shape() {
    let $ = { ...Q._zod.def.shape, ...X };
    return IQ(this, "shape", $), $;
  }, checks: [] };
  return l0(Q, Y);
}
function oq(Q, X) {
  return l0(Q, { ...Q._zod.def, get shape() {
    let Y = { ...Q._zod.def.shape, ...X._zod.def.shape };
    return IQ(this, "shape", Y), Y;
  }, catchall: X._zod.def.catchall, checks: [] });
}
function tq(Q, X, Y) {
  let $ = X._zod.def.shape, W = { ...$ };
  if (Y) for (let J in Y) {
    if (!(J in $)) throw Error(`Unrecognized key: "${J}"`);
    if (!Y[J]) continue;
    W[J] = Q ? new Q({ type: "optional", innerType: $[J] }) : $[J];
  }
  else for (let J in $) W[J] = Q ? new Q({ type: "optional", innerType: $[J] }) : $[J];
  return l0(X, { ...X._zod.def, shape: W, checks: [] });
}
function aq(Q, X, Y) {
  let $ = X._zod.def.shape, W = { ...$ };
  if (Y) for (let J in Y) {
    if (!(J in W)) throw Error(`Unrecognized key: "${J}"`);
    if (!Y[J]) continue;
    W[J] = new Q({ type: "nonoptional", innerType: $[J] });
  }
  else for (let J in $) W[J] = new Q({ type: "nonoptional", innerType: $[J] });
  return l0(X, { ...X._zod.def, shape: W, checks: [] });
}
function X6(Q, X = 0) {
  for (let Y = X; Y < Q.issues.length; Y++) if (Q.issues[Y]?.continue !== true) return true;
  return false;
}
function H1(Q, X) {
  return X.map((Y) => {
    var $;
    return ($ = Y).path ?? ($.path = []), Y.path.unshift(Q), Y;
  });
}
function D4(Q) {
  return typeof Q === "string" ? Q : Q?.message;
}
function r0(Q, X, Y) {
  let $ = { ...Q, path: Q.path ?? [] };
  if (!Q.message) {
    let W = D4(Q.inst?._zod.def?.error?.(Q)) ?? D4(X?.error?.(Q)) ?? D4(Y.customError?.(Q)) ?? D4(Y.localeError?.(Q)) ?? "Invalid input";
    $.message = W;
  }
  if (delete $.inst, delete $.continue, !X?.reportInput) delete $.input;
  return $;
}
function wW(Q) {
  if (Q instanceof Set) return "set";
  if (Q instanceof Map) return "map";
  if (Q instanceof File) return "file";
  return "unknown";
}
function j4(Q) {
  if (Array.isArray(Q)) return "array";
  if (typeof Q === "string") return "string";
  return "unknown";
}
function ZQ(...Q) {
  let [X, Y, $] = Q;
  if (typeof X === "string") return { message: X, code: "custom", input: Y, inst: $ };
  return { ...X };
}
function sq(Q) {
  return Object.entries(Q).filter(([X, Y]) => {
    return Number.isNaN(Number.parseInt(X, 10));
  }).map((X) => X[1]);
}
var MW = class {
  constructor(...Q) {
  }
};
var AW = (Q, X) => {
  Q.name = "$ZodError", Object.defineProperty(Q, "_zod", { value: Q._zod, enumerable: false }), Object.defineProperty(Q, "issues", { value: X, enumerable: false }), Object.defineProperty(Q, "message", { get() {
    return JSON.stringify(X, jQ, 2);
  }, enumerable: true });
};
var C9 = D("$ZodError", AW);
var R4 = D("$ZodError", AW, { Parent: Error });
function CQ(Q, X = (Y) => Y.message) {
  let Y = {}, $ = [];
  for (let W of Q.issues) if (W.path.length > 0) Y[W.path[0]] = Y[W.path[0]] || [], Y[W.path[0]].push(X(W));
  else $.push(X(W));
  return { formErrors: $, fieldErrors: Y };
}
function kQ(Q, X) {
  let Y = X || function(J) {
    return J.message;
  }, $ = { _errors: [] }, W = (J) => {
    for (let G of J.issues) if (G.code === "invalid_union" && G.errors.length) G.errors.map((H) => W({ issues: H }));
    else if (G.code === "invalid_key") W({ issues: G.issues });
    else if (G.code === "invalid_element") W({ issues: G.issues });
    else if (G.path.length === 0) $._errors.push(Y(G));
    else {
      let H = $, B = 0;
      while (B < G.path.length) {
        let z = G.path[B];
        if (B !== G.path.length - 1) H[z] = H[z] || { _errors: [] };
        else H[z] = H[z] || { _errors: [] }, H[z]._errors.push(Y(G));
        H = H[z], B++;
      }
    }
  };
  return W(Q), $;
}
var vQ = (Q) => (X, Y, $, W) => {
  let J = $ ? Object.assign($, { async: false }) : { async: false }, G = X._zod.run({ value: Y, issues: [] }, J);
  if (G instanceof Promise) throw new x1();
  if (G.issues.length) {
    let H = new (W?.Err ?? Q)(G.issues.map((B) => r0(B, J, u0())));
    throw S9(H, W?.callee), H;
  }
  return G.value;
};
var _Q = vQ(R4);
var TQ = (Q) => async (X, Y, $, W) => {
  let J = $ ? Object.assign($, { async: true }) : { async: true }, G = X._zod.run({ value: Y, issues: [] }, J);
  if (G instanceof Promise) G = await G;
  if (G.issues.length) {
    let H = new (W?.Err ?? Q)(G.issues.map((B) => r0(B, J, u0())));
    throw S9(H, W?.callee), H;
  }
  return G.value;
};
var xQ = TQ(R4);
var yQ = (Q) => (X, Y, $) => {
  let W = $ ? { ...$, async: false } : { async: false }, J = X._zod.run({ value: Y, issues: [] }, W);
  if (J instanceof Promise) throw new x1();
  return J.issues.length ? { success: false, error: new (Q ?? C9)(J.issues.map((G) => r0(G, W, u0()))) } : { success: true, data: J.value };
};
var Y6 = yQ(R4);
var gQ = (Q) => async (X, Y, $) => {
  let W = $ ? Object.assign($, { async: true }) : { async: true }, J = X._zod.run({ value: Y, issues: [] }, W);
  if (J instanceof Promise) J = await J;
  return J.issues.length ? { success: false, error: new Q(J.issues.map((G) => r0(G, W, u0()))) } : { success: true, data: J.value };
};
var $6 = gQ(R4);
var jW = /^[cC][^\s-]{8,}$/;
var RW = /^[0-9a-z]+$/;
var IW = /^[0-9A-HJKMNP-TV-Za-hjkmnp-tv-z]{26}$/;
var EW = /^[0-9a-vA-V]{20}$/;
var bW = /^[A-Za-z0-9]{27}$/;
var PW = /^[a-zA-Z0-9_-]{21}$/;
var SW = /^P(?:(\d+W)|(?!.*W)(?=\d|T\d)(\d+Y)?(\d+M)?(\d+D)?(T(?=\d)(\d+H)?(\d+M)?(\d+([.,]\d+)?S)?)?)$/;
var ZW = /^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})$/;
var hQ = (Q) => {
  if (!Q) return /^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}|00000000-0000-0000-0000-000000000000)$/;
  return new RegExp(`^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-${Q}[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12})$`);
};
var CW = /^(?!\.)(?!.*\.\.)([A-Za-z0-9_'+\-\.]*)[A-Za-z0-9_+-]@([A-Za-z0-9][A-Za-z0-9\-]*\.)+[A-Za-z]{2,}$/;
function kW() {
  return new RegExp("^(\\p{Extended_Pictographic}|\\p{Emoji_Component})+$", "u");
}
var vW = /^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$/;
var _W = /^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::|([0-9a-fA-F]{1,4})?::([0-9a-fA-F]{1,4}:?){0,6})$/;
var TW = /^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\/([0-9]|[1-2][0-9]|3[0-2])$/;
var xW = /^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::|([0-9a-fA-F]{1,4})?::([0-9a-fA-F]{1,4}:?){0,6})\/(12[0-8]|1[01][0-9]|[1-9]?[0-9])$/;
var yW = /^$|^(?:[0-9a-zA-Z+/]{4})*(?:(?:[0-9a-zA-Z+/]{2}==)|(?:[0-9a-zA-Z+/]{3}=))?$/;
var fQ = /^[A-Za-z0-9_-]*$/;
var gW = /^([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+$/;
var hW = /^\+(?:[0-9]){6,14}[0-9]$/;
var fW = "(?:(?:\\d\\d[2468][048]|\\d\\d[13579][26]|\\d\\d0[48]|[02468][048]00|[13579][26]00)-02-29|\\d{4}-(?:(?:0[13578]|1[02])-(?:0[1-9]|[12]\\d|3[01])|(?:0[469]|11)-(?:0[1-9]|[12]\\d|30)|(?:02)-(?:0[1-9]|1\\d|2[0-8])))";
var uW = new RegExp(`^${fW}$`);
function lW(Q) {
  return typeof Q.precision === "number" ? Q.precision === -1 ? "(?:[01]\\d|2[0-3]):[0-5]\\d" : Q.precision === 0 ? "(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d" : `(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{${Q.precision}}` : "(?:[01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d(?:\\.\\d+)?)?";
}
function mW(Q) {
  return new RegExp(`^${lW(Q)}$`);
}
function cW(Q) {
  let X = lW({ precision: Q.precision }), Y = ["Z"];
  if (Q.local) Y.push("");
  if (Q.offset) Y.push("([+-]\\d{2}:\\d{2})");
  let $ = `${X}(?:${Y.join("|")})`;
  return new RegExp(`^${fW}T(?:${$})$`);
}
var pW = (Q) => {
  let X = Q ? `[\\s\\S]{${Q?.minimum ?? 0},${Q?.maximum ?? ""}}` : "[\\s\\S]*";
  return new RegExp(`^${X}$`);
};
var dW = /^\d+$/;
var iW = /^-?\d+(?:\.\d+)?/i;
var nW = /true|false/i;
var rW = /null/i;
var oW = /^[^A-Z]*$/;
var tW = /^[^a-z]*$/;
var M0 = D("$ZodCheck", (Q, X) => {
  var Y;
  Q._zod ?? (Q._zod = {}), Q._zod.def = X, (Y = Q._zod).onattach ?? (Y.onattach = []);
});
var aW = { number: "number", bigint: "bigint", object: "date" };
var uQ = D("$ZodCheckLessThan", (Q, X) => {
  M0.init(Q, X);
  let Y = aW[typeof X.value];
  Q._zod.onattach.push(($) => {
    let W = $._zod.bag, J = (X.inclusive ? W.maximum : W.exclusiveMaximum) ?? Number.POSITIVE_INFINITY;
    if (X.value < J) if (X.inclusive) W.maximum = X.value;
    else W.exclusiveMaximum = X.value;
  }), Q._zod.check = ($) => {
    if (X.inclusive ? $.value <= X.value : $.value < X.value) return;
    $.issues.push({ origin: Y, code: "too_big", maximum: X.value, input: $.value, inclusive: X.inclusive, inst: Q, continue: !X.abort });
  };
});
var lQ = D("$ZodCheckGreaterThan", (Q, X) => {
  M0.init(Q, X);
  let Y = aW[typeof X.value];
  Q._zod.onattach.push(($) => {
    let W = $._zod.bag, J = (X.inclusive ? W.minimum : W.exclusiveMinimum) ?? Number.NEGATIVE_INFINITY;
    if (X.value > J) if (X.inclusive) W.minimum = X.value;
    else W.exclusiveMinimum = X.value;
  }), Q._zod.check = ($) => {
    if (X.inclusive ? $.value >= X.value : $.value > X.value) return;
    $.issues.push({ origin: Y, code: "too_small", minimum: X.value, input: $.value, inclusive: X.inclusive, inst: Q, continue: !X.abort });
  };
});
var sW = D("$ZodCheckMultipleOf", (Q, X) => {
  M0.init(Q, X), Q._zod.onattach.push((Y) => {
    var $;
    ($ = Y._zod.bag).multipleOf ?? ($.multipleOf = X.value);
  }), Q._zod.check = (Y) => {
    if (typeof Y.value !== typeof X.value) throw Error("Cannot mix number and bigint in multiple_of check.");
    if (typeof Y.value === "bigint" ? Y.value % X.value === BigInt(0) : RQ(Y.value, X.value) === 0) return;
    Y.issues.push({ origin: typeof Y.value, code: "not_multiple_of", divisor: X.value, input: Y.value, inst: Q, continue: !X.abort });
  };
});
var eW = D("$ZodCheckNumberFormat", (Q, X) => {
  M0.init(Q, X), X.format = X.format || "float64";
  let Y = X.format?.includes("int"), $ = Y ? "int" : "number", [W, J] = SQ[X.format];
  Q._zod.onattach.push((G) => {
    let H = G._zod.bag;
    if (H.format = X.format, H.minimum = W, H.maximum = J, Y) H.pattern = dW;
  }), Q._zod.check = (G) => {
    let H = G.value;
    if (Y) {
      if (!Number.isInteger(H)) {
        G.issues.push({ expected: $, format: X.format, code: "invalid_type", input: H, inst: Q });
        return;
      }
      if (!Number.isSafeInteger(H)) {
        if (H > 0) G.issues.push({ input: H, code: "too_big", maximum: Number.MAX_SAFE_INTEGER, note: "Integers must be within the safe integer range.", inst: Q, origin: $, continue: !X.abort });
        else G.issues.push({ input: H, code: "too_small", minimum: Number.MIN_SAFE_INTEGER, note: "Integers must be within the safe integer range.", inst: Q, origin: $, continue: !X.abort });
        return;
      }
    }
    if (H < W) G.issues.push({ origin: "number", input: H, code: "too_small", minimum: W, inclusive: true, inst: Q, continue: !X.abort });
    if (H > J) G.issues.push({ origin: "number", input: H, code: "too_big", maximum: J, inst: Q });
  };
});
var QJ = D("$ZodCheckMaxLength", (Q, X) => {
  M0.init(Q, X), Q._zod.when = (Y) => {
    let $ = Y.value;
    return !M4($) && $.length !== void 0;
  }, Q._zod.onattach.push((Y) => {
    let $ = Y._zod.bag.maximum ?? Number.POSITIVE_INFINITY;
    if (X.maximum < $) Y._zod.bag.maximum = X.maximum;
  }), Q._zod.check = (Y) => {
    let $ = Y.value;
    if ($.length <= X.maximum) return;
    let J = j4($);
    Y.issues.push({ origin: J, code: "too_big", maximum: X.maximum, inclusive: true, input: $, inst: Q, continue: !X.abort });
  };
});
var XJ = D("$ZodCheckMinLength", (Q, X) => {
  M0.init(Q, X), Q._zod.when = (Y) => {
    let $ = Y.value;
    return !M4($) && $.length !== void 0;
  }, Q._zod.onattach.push((Y) => {
    let $ = Y._zod.bag.minimum ?? Number.NEGATIVE_INFINITY;
    if (X.minimum > $) Y._zod.bag.minimum = X.minimum;
  }), Q._zod.check = (Y) => {
    let $ = Y.value;
    if ($.length >= X.minimum) return;
    let J = j4($);
    Y.issues.push({ origin: J, code: "too_small", minimum: X.minimum, inclusive: true, input: $, inst: Q, continue: !X.abort });
  };
});
var YJ = D("$ZodCheckLengthEquals", (Q, X) => {
  M0.init(Q, X), Q._zod.when = (Y) => {
    let $ = Y.value;
    return !M4($) && $.length !== void 0;
  }, Q._zod.onattach.push((Y) => {
    let $ = Y._zod.bag;
    $.minimum = X.length, $.maximum = X.length, $.length = X.length;
  }), Q._zod.check = (Y) => {
    let $ = Y.value, W = $.length;
    if (W === X.length) return;
    let J = j4($), G = W > X.length;
    Y.issues.push({ origin: J, ...G ? { code: "too_big", maximum: X.length } : { code: "too_small", minimum: X.length }, inclusive: true, exact: true, input: Y.value, inst: Q, continue: !X.abort });
  };
});
var I4 = D("$ZodCheckStringFormat", (Q, X) => {
  var Y, $;
  if (M0.init(Q, X), Q._zod.onattach.push((W) => {
    let J = W._zod.bag;
    if (J.format = X.format, X.pattern) J.patterns ?? (J.patterns = /* @__PURE__ */ new Set()), J.patterns.add(X.pattern);
  }), X.pattern) (Y = Q._zod).check ?? (Y.check = (W) => {
    if (X.pattern.lastIndex = 0, X.pattern.test(W.value)) return;
    W.issues.push({ origin: "string", code: "invalid_format", format: X.format, input: W.value, ...X.pattern ? { pattern: X.pattern.toString() } : {}, inst: Q, continue: !X.abort });
  });
  else ($ = Q._zod).check ?? ($.check = () => {
  });
});
var $J = D("$ZodCheckRegex", (Q, X) => {
  I4.init(Q, X), Q._zod.check = (Y) => {
    if (X.pattern.lastIndex = 0, X.pattern.test(Y.value)) return;
    Y.issues.push({ origin: "string", code: "invalid_format", format: "regex", input: Y.value, pattern: X.pattern.toString(), inst: Q, continue: !X.abort });
  };
});
var WJ = D("$ZodCheckLowerCase", (Q, X) => {
  X.pattern ?? (X.pattern = oW), I4.init(Q, X);
});
var JJ = D("$ZodCheckUpperCase", (Q, X) => {
  X.pattern ?? (X.pattern = tW), I4.init(Q, X);
});
var GJ = D("$ZodCheckIncludes", (Q, X) => {
  M0.init(Q, X);
  let Y = y1(X.includes), $ = new RegExp(typeof X.position === "number" ? `^.{${X.position}}${Y}` : Y);
  X.pattern = $, Q._zod.onattach.push((W) => {
    let J = W._zod.bag;
    J.patterns ?? (J.patterns = /* @__PURE__ */ new Set()), J.patterns.add($);
  }), Q._zod.check = (W) => {
    if (W.value.includes(X.includes, X.position)) return;
    W.issues.push({ origin: "string", code: "invalid_format", format: "includes", includes: X.includes, input: W.value, inst: Q, continue: !X.abort });
  };
});
var HJ = D("$ZodCheckStartsWith", (Q, X) => {
  M0.init(Q, X);
  let Y = new RegExp(`^${y1(X.prefix)}.*`);
  X.pattern ?? (X.pattern = Y), Q._zod.onattach.push(($) => {
    let W = $._zod.bag;
    W.patterns ?? (W.patterns = /* @__PURE__ */ new Set()), W.patterns.add(Y);
  }), Q._zod.check = ($) => {
    if ($.value.startsWith(X.prefix)) return;
    $.issues.push({ origin: "string", code: "invalid_format", format: "starts_with", prefix: X.prefix, input: $.value, inst: Q, continue: !X.abort });
  };
});
var BJ = D("$ZodCheckEndsWith", (Q, X) => {
  M0.init(Q, X);
  let Y = new RegExp(`.*${y1(X.suffix)}$`);
  X.pattern ?? (X.pattern = Y), Q._zod.onattach.push(($) => {
    let W = $._zod.bag;
    W.patterns ?? (W.patterns = /* @__PURE__ */ new Set()), W.patterns.add(Y);
  }), Q._zod.check = ($) => {
    if ($.value.endsWith(X.suffix)) return;
    $.issues.push({ origin: "string", code: "invalid_format", format: "ends_with", suffix: X.suffix, input: $.value, inst: Q, continue: !X.abort });
  };
});
var zJ = D("$ZodCheckOverwrite", (Q, X) => {
  M0.init(Q, X), Q._zod.check = (Y) => {
    Y.value = X.tx(Y.value);
  };
});
var mQ = class {
  constructor(Q = []) {
    if (this.content = [], this.indent = 0, this) this.args = Q;
  }
  indented(Q) {
    this.indent += 1, Q(this), this.indent -= 1;
  }
  write(Q) {
    if (typeof Q === "function") {
      Q(this, { execution: "sync" }), Q(this, { execution: "async" });
      return;
    }
    let Y = Q.split(`
`).filter((J) => J), $ = Math.min(...Y.map((J) => J.length - J.trimStart().length)), W = Y.map((J) => J.slice($)).map((J) => " ".repeat(this.indent * 2) + J);
    for (let J of W) this.content.push(J);
  }
  compile() {
    let Q = Function, X = this?.args, $ = [...(this?.content ?? [""]).map((W) => `  ${W}`)];
    return new Q(...X, $.join(`
`));
  }
};
var VJ = { major: 4, minor: 0, patch: 0 };
var Q0 = D("$ZodType", (Q, X) => {
  var Y;
  Q ?? (Q = {}), Q._zod.def = X, Q._zod.bag = Q._zod.bag || {}, Q._zod.version = VJ;
  let $ = [...Q._zod.def.checks ?? []];
  if (Q._zod.traits.has("$ZodCheck")) $.unshift(Q);
  for (let W of $) for (let J of W._zod.onattach) J(Q);
  if ($.length === 0) (Y = Q._zod).deferred ?? (Y.deferred = []), Q._zod.deferred?.push(() => {
    Q._zod.run = Q._zod.parse;
  });
  else {
    let W = (J, G, H) => {
      let B = X6(J), z;
      for (let K of G) {
        if (K._zod.when) {
          if (!K._zod.when(J)) continue;
        } else if (B) continue;
        let q = J.issues.length, U = K._zod.check(J);
        if (U instanceof Promise && H?.async === false) throw new x1();
        if (z || U instanceof Promise) z = (z ?? Promise.resolve()).then(async () => {
          if (await U, J.issues.length === q) return;
          if (!B) B = X6(J, q);
        });
        else {
          if (J.issues.length === q) continue;
          if (!B) B = X6(J, q);
        }
      }
      if (z) return z.then(() => {
        return J;
      });
      return J;
    };
    Q._zod.run = (J, G) => {
      let H = Q._zod.parse(J, G);
      if (H instanceof Promise) {
        if (G.async === false) throw new x1();
        return H.then((B) => W(B, $, G));
      }
      return W(H, $, G);
    };
  }
  Q["~standard"] = { validate: (W) => {
    try {
      let J = Y6(Q, W);
      return J.success ? { value: J.data } : { issues: J.error?.issues };
    } catch (J) {
      return $6(Q, W).then((G) => G.success ? { value: G.data } : { issues: G.error?.issues });
    }
  }, vendor: "zod", version: 1 };
});
var E4 = D("$ZodString", (Q, X) => {
  Q0.init(Q, X), Q._zod.pattern = [...Q?._zod.bag?.patterns ?? []].pop() ?? pW(Q._zod.bag), Q._zod.parse = (Y, $) => {
    if (X.coerce) try {
      Y.value = String(Y.value);
    } catch (W) {
    }
    if (typeof Y.value === "string") return Y;
    return Y.issues.push({ expected: "string", code: "invalid_type", input: Y.value, inst: Q }), Y;
  };
});
var W0 = D("$ZodStringFormat", (Q, X) => {
  I4.init(Q, X), E4.init(Q, X);
});
var pQ = D("$ZodGUID", (Q, X) => {
  X.pattern ?? (X.pattern = ZW), W0.init(Q, X);
});
var dQ = D("$ZodUUID", (Q, X) => {
  if (X.version) {
    let $ = { v1: 1, v2: 2, v3: 3, v4: 4, v5: 5, v6: 6, v7: 7, v8: 8 }[X.version];
    if ($ === void 0) throw Error(`Invalid UUID version: "${X.version}"`);
    X.pattern ?? (X.pattern = hQ($));
  } else X.pattern ?? (X.pattern = hQ());
  W0.init(Q, X);
});
var iQ = D("$ZodEmail", (Q, X) => {
  X.pattern ?? (X.pattern = CW), W0.init(Q, X);
});
var nQ = D("$ZodURL", (Q, X) => {
  W0.init(Q, X), Q._zod.check = (Y) => {
    try {
      let $ = Y.value, W = new URL($), J = W.href;
      if (X.hostname) {
        if (X.hostname.lastIndex = 0, !X.hostname.test(W.hostname)) Y.issues.push({ code: "invalid_format", format: "url", note: "Invalid hostname", pattern: gW.source, input: Y.value, inst: Q, continue: !X.abort });
      }
      if (X.protocol) {
        if (X.protocol.lastIndex = 0, !X.protocol.test(W.protocol.endsWith(":") ? W.protocol.slice(0, -1) : W.protocol)) Y.issues.push({ code: "invalid_format", format: "url", note: "Invalid protocol", pattern: X.protocol.source, input: Y.value, inst: Q, continue: !X.abort });
      }
      if (!$.endsWith("/") && J.endsWith("/")) Y.value = J.slice(0, -1);
      else Y.value = J;
      return;
    } catch ($) {
      Y.issues.push({ code: "invalid_format", format: "url", input: Y.value, inst: Q, continue: !X.abort });
    }
  };
});
var rQ = D("$ZodEmoji", (Q, X) => {
  X.pattern ?? (X.pattern = kW()), W0.init(Q, X);
});
var oQ = D("$ZodNanoID", (Q, X) => {
  X.pattern ?? (X.pattern = PW), W0.init(Q, X);
});
var tQ = D("$ZodCUID", (Q, X) => {
  X.pattern ?? (X.pattern = jW), W0.init(Q, X);
});
var aQ = D("$ZodCUID2", (Q, X) => {
  X.pattern ?? (X.pattern = RW), W0.init(Q, X);
});
var sQ = D("$ZodULID", (Q, X) => {
  X.pattern ?? (X.pattern = IW), W0.init(Q, X);
});
var eQ = D("$ZodXID", (Q, X) => {
  X.pattern ?? (X.pattern = EW), W0.init(Q, X);
});
var QX = D("$ZodKSUID", (Q, X) => {
  X.pattern ?? (X.pattern = bW), W0.init(Q, X);
});
var AJ = D("$ZodISODateTime", (Q, X) => {
  X.pattern ?? (X.pattern = cW(X)), W0.init(Q, X);
});
var jJ = D("$ZodISODate", (Q, X) => {
  X.pattern ?? (X.pattern = uW), W0.init(Q, X);
});
var RJ = D("$ZodISOTime", (Q, X) => {
  X.pattern ?? (X.pattern = mW(X)), W0.init(Q, X);
});
var IJ = D("$ZodISODuration", (Q, X) => {
  X.pattern ?? (X.pattern = SW), W0.init(Q, X);
});
var XX = D("$ZodIPv4", (Q, X) => {
  X.pattern ?? (X.pattern = vW), W0.init(Q, X), Q._zod.onattach.push((Y) => {
    let $ = Y._zod.bag;
    $.format = "ipv4";
  });
});
var YX = D("$ZodIPv6", (Q, X) => {
  X.pattern ?? (X.pattern = _W), W0.init(Q, X), Q._zod.onattach.push((Y) => {
    let $ = Y._zod.bag;
    $.format = "ipv6";
  }), Q._zod.check = (Y) => {
    try {
      new URL(`http://[${Y.value}]`);
    } catch {
      Y.issues.push({ code: "invalid_format", format: "ipv6", input: Y.value, inst: Q, continue: !X.abort });
    }
  };
});
var $X = D("$ZodCIDRv4", (Q, X) => {
  X.pattern ?? (X.pattern = TW), W0.init(Q, X);
});
var WX = D("$ZodCIDRv6", (Q, X) => {
  X.pattern ?? (X.pattern = xW), W0.init(Q, X), Q._zod.check = (Y) => {
    let [$, W] = Y.value.split("/");
    try {
      if (!W) throw Error();
      let J = Number(W);
      if (`${J}` !== W) throw Error();
      if (J < 0 || J > 128) throw Error();
      new URL(`http://[${$}]`);
    } catch {
      Y.issues.push({ code: "invalid_format", format: "cidrv6", input: Y.value, inst: Q, continue: !X.abort });
    }
  };
});
function EJ(Q) {
  if (Q === "") return true;
  if (Q.length % 4 !== 0) return false;
  try {
    return atob(Q), true;
  } catch {
    return false;
  }
}
var JX = D("$ZodBase64", (Q, X) => {
  X.pattern ?? (X.pattern = yW), W0.init(Q, X), Q._zod.onattach.push((Y) => {
    Y._zod.bag.contentEncoding = "base64";
  }), Q._zod.check = (Y) => {
    if (EJ(Y.value)) return;
    Y.issues.push({ code: "invalid_format", format: "base64", input: Y.value, inst: Q, continue: !X.abort });
  };
});
function QU(Q) {
  if (!fQ.test(Q)) return false;
  let X = Q.replace(/[-_]/g, ($) => $ === "-" ? "+" : "/"), Y = X.padEnd(Math.ceil(X.length / 4) * 4, "=");
  return EJ(Y);
}
var GX = D("$ZodBase64URL", (Q, X) => {
  X.pattern ?? (X.pattern = fQ), W0.init(Q, X), Q._zod.onattach.push((Y) => {
    Y._zod.bag.contentEncoding = "base64url";
  }), Q._zod.check = (Y) => {
    if (QU(Y.value)) return;
    Y.issues.push({ code: "invalid_format", format: "base64url", input: Y.value, inst: Q, continue: !X.abort });
  };
});
var HX = D("$ZodE164", (Q, X) => {
  X.pattern ?? (X.pattern = hW), W0.init(Q, X);
});
function XU(Q, X = null) {
  try {
    let Y = Q.split(".");
    if (Y.length !== 3) return false;
    let [$] = Y;
    if (!$) return false;
    let W = JSON.parse(atob($));
    if ("typ" in W && W?.typ !== "JWT") return false;
    if (!W.alg) return false;
    if (X && (!("alg" in W) || W.alg !== X)) return false;
    return true;
  } catch {
    return false;
  }
}
var BX = D("$ZodJWT", (Q, X) => {
  W0.init(Q, X), Q._zod.check = (Y) => {
    if (XU(Y.value, X.alg)) return;
    Y.issues.push({ code: "invalid_format", format: "jwt", input: Y.value, inst: Q, continue: !X.abort });
  };
});
var _9 = D("$ZodNumber", (Q, X) => {
  Q0.init(Q, X), Q._zod.pattern = Q._zod.bag.pattern ?? iW, Q._zod.parse = (Y, $) => {
    if (X.coerce) try {
      Y.value = Number(Y.value);
    } catch (G) {
    }
    let W = Y.value;
    if (typeof W === "number" && !Number.isNaN(W) && Number.isFinite(W)) return Y;
    let J = typeof W === "number" ? Number.isNaN(W) ? "NaN" : !Number.isFinite(W) ? "Infinity" : void 0 : void 0;
    return Y.issues.push({ expected: "number", code: "invalid_type", input: W, inst: Q, ...J ? { received: J } : {} }), Y;
  };
});
var zX = D("$ZodNumber", (Q, X) => {
  eW.init(Q, X), _9.init(Q, X);
});
var KX = D("$ZodBoolean", (Q, X) => {
  Q0.init(Q, X), Q._zod.pattern = nW, Q._zod.parse = (Y, $) => {
    if (X.coerce) try {
      Y.value = Boolean(Y.value);
    } catch (J) {
    }
    let W = Y.value;
    if (typeof W === "boolean") return Y;
    return Y.issues.push({ expected: "boolean", code: "invalid_type", input: W, inst: Q }), Y;
  };
});
var VX = D("$ZodNull", (Q, X) => {
  Q0.init(Q, X), Q._zod.pattern = rW, Q._zod.values = /* @__PURE__ */ new Set([null]), Q._zod.parse = (Y, $) => {
    let W = Y.value;
    if (W === null) return Y;
    return Y.issues.push({ expected: "null", code: "invalid_type", input: W, inst: Q }), Y;
  };
});
var qX = D("$ZodUnknown", (Q, X) => {
  Q0.init(Q, X), Q._zod.parse = (Y) => Y;
});
var UX = D("$ZodNever", (Q, X) => {
  Q0.init(Q, X), Q._zod.parse = (Y, $) => {
    return Y.issues.push({ expected: "never", code: "invalid_type", input: Y.value, inst: Q }), Y;
  };
});
function qJ(Q, X, Y) {
  if (Q.issues.length) X.issues.push(...H1(Y, Q.issues));
  X.value[Y] = Q.value;
}
var LX = D("$ZodArray", (Q, X) => {
  Q0.init(Q, X), Q._zod.parse = (Y, $) => {
    let W = Y.value;
    if (!Array.isArray(W)) return Y.issues.push({ expected: "array", code: "invalid_type", input: W, inst: Q }), Y;
    Y.value = Array(W.length);
    let J = [];
    for (let G = 0; G < W.length; G++) {
      let H = W[G], B = X.element._zod.run({ value: H, issues: [] }, $);
      if (B instanceof Promise) J.push(B.then((z) => qJ(z, Y, G)));
      else qJ(B, Y, G);
    }
    if (J.length) return Promise.all(J).then(() => Y);
    return Y;
  };
});
function v9(Q, X, Y) {
  if (Q.issues.length) X.issues.push(...H1(Y, Q.issues));
  X.value[Y] = Q.value;
}
function UJ(Q, X, Y, $) {
  if (Q.issues.length) if ($[Y] === void 0) if (Y in $) X.value[Y] = void 0;
  else X.value[Y] = Q.value;
  else X.issues.push(...H1(Y, Q.issues));
  else if (Q.value === void 0) {
    if (Y in $) X.value[Y] = void 0;
  } else X.value[Y] = Q.value;
}
var T9 = D("$ZodObject", (Q, X) => {
  Q0.init(Q, X);
  let Y = w4(() => {
    let q = Object.keys(X.shape);
    for (let V of q) if (!(X.shape[V] instanceof Q0)) throw Error(`Invalid element at key "${V}": expected a Zod schema`);
    let U = PQ(X.shape);
    return { shape: X.shape, keys: q, keySet: new Set(q), numKeys: q.length, optionalKeys: new Set(U) };
  });
  $0(Q._zod, "propValues", () => {
    let q = X.shape, U = {};
    for (let V in q) {
      let F = q[V]._zod;
      if (F.values) {
        U[V] ?? (U[V] = /* @__PURE__ */ new Set());
        for (let L of F.values) U[V].add(L);
      }
    }
    return U;
  });
  let $ = (q) => {
    let U = new mQ(["shape", "payload", "ctx"]), V = Y.value, F = (A) => {
      let R = Q6(A);
      return `shape[${R}]._zod.run({ value: input[${R}], issues: [] }, ctx)`;
    };
    U.write("const input = payload.value;");
    let L = /* @__PURE__ */ Object.create(null), N = 0;
    for (let A of V.keys) L[A] = `key_${N++}`;
    U.write("const newResult = {}");
    for (let A of V.keys) if (V.optionalKeys.has(A)) {
      let R = L[A];
      U.write(`const ${R} = ${F(A)};`);
      let S = Q6(A);
      U.write(`
        if (${R}.issues.length) {
          if (input[${S}] === undefined) {
            if (${S} in input) {
              newResult[${S}] = undefined;
            }
          } else {
            payload.issues = payload.issues.concat(
              ${R}.issues.map((iss) => ({
                ...iss,
                path: iss.path ? [${S}, ...iss.path] : [${S}],
              }))
            );
          }
        } else if (${R}.value === undefined) {
          if (${S} in input) newResult[${S}] = undefined;
        } else {
          newResult[${S}] = ${R}.value;
        }
        `);
    } else {
      let R = L[A];
      U.write(`const ${R} = ${F(A)};`), U.write(`
          if (${R}.issues.length) payload.issues = payload.issues.concat(${R}.issues.map(iss => ({
            ...iss,
            path: iss.path ? [${Q6(A)}, ...iss.path] : [${Q6(A)}]
          })));`), U.write(`newResult[${Q6(A)}] = ${R}.value`);
    }
    U.write("payload.value = newResult;"), U.write("return payload;");
    let w = U.compile();
    return (A, R) => w(q, A, R);
  }, W, J = C6, G = !b9.jitless, B = G && EQ.value, z = X.catchall, K;
  Q._zod.parse = (q, U) => {
    K ?? (K = Y.value);
    let V = q.value;
    if (!J(V)) return q.issues.push({ expected: "object", code: "invalid_type", input: V, inst: Q }), q;
    let F = [];
    if (G && B && U?.async === false && U.jitless !== true) {
      if (!W) W = $(X.shape);
      q = W(q, U);
    } else {
      q.value = {};
      let R = K.shape;
      for (let S of K.keys) {
        let C = R[S], K0 = C._zod.run({ value: V[S], issues: [] }, U), V0 = C._zod.optin === "optional" && C._zod.optout === "optional";
        if (K0 instanceof Promise) F.push(K0.then((s) => V0 ? UJ(s, q, S, V) : v9(s, q, S)));
        else if (V0) UJ(K0, q, S, V);
        else v9(K0, q, S);
      }
    }
    if (!z) return F.length ? Promise.all(F).then(() => q) : q;
    let L = [], N = K.keySet, w = z._zod, A = w.def.type;
    for (let R of Object.keys(V)) {
      if (N.has(R)) continue;
      if (A === "never") {
        L.push(R);
        continue;
      }
      let S = w.run({ value: V[R], issues: [] }, U);
      if (S instanceof Promise) F.push(S.then((C) => v9(C, q, R)));
      else v9(S, q, R);
    }
    if (L.length) q.issues.push({ code: "unrecognized_keys", keys: L, input: V, inst: Q });
    if (!F.length) return q;
    return Promise.all(F).then(() => {
      return q;
    });
  };
});
function LJ(Q, X, Y, $) {
  for (let W of Q) if (W.issues.length === 0) return X.value = W.value, X;
  return X.issues.push({ code: "invalid_union", input: X.value, inst: Y, errors: Q.map((W) => W.issues.map((J) => r0(J, $, u0()))) }), X;
}
var x9 = D("$ZodUnion", (Q, X) => {
  Q0.init(Q, X), $0(Q._zod, "optin", () => X.options.some((Y) => Y._zod.optin === "optional") ? "optional" : void 0), $0(Q._zod, "optout", () => X.options.some((Y) => Y._zod.optout === "optional") ? "optional" : void 0), $0(Q._zod, "values", () => {
    if (X.options.every((Y) => Y._zod.values)) return new Set(X.options.flatMap((Y) => Array.from(Y._zod.values)));
    return;
  }), $0(Q._zod, "pattern", () => {
    if (X.options.every((Y) => Y._zod.pattern)) {
      let Y = X.options.map(($) => $._zod.pattern);
      return new RegExp(`^(${Y.map(($) => A4($.source)).join("|")})$`);
    }
    return;
  }), Q._zod.parse = (Y, $) => {
    let W = false, J = [];
    for (let G of X.options) {
      let H = G._zod.run({ value: Y.value, issues: [] }, $);
      if (H instanceof Promise) J.push(H), W = true;
      else {
        if (H.issues.length === 0) return H;
        J.push(H);
      }
    }
    if (!W) return LJ(J, Y, Q, $);
    return Promise.all(J).then((G) => {
      return LJ(G, Y, Q, $);
    });
  };
});
var FX = D("$ZodDiscriminatedUnion", (Q, X) => {
  x9.init(Q, X);
  let Y = Q._zod.parse;
  $0(Q._zod, "propValues", () => {
    let W = {};
    for (let J of X.options) {
      let G = J._zod.propValues;
      if (!G || Object.keys(G).length === 0) throw Error(`Invalid discriminated union option at index "${X.options.indexOf(J)}"`);
      for (let [H, B] of Object.entries(G)) {
        if (!W[H]) W[H] = /* @__PURE__ */ new Set();
        for (let z of B) W[H].add(z);
      }
    }
    return W;
  });
  let $ = w4(() => {
    let W = X.options, J = /* @__PURE__ */ new Map();
    for (let G of W) {
      let H = G._zod.propValues[X.discriminator];
      if (!H || H.size === 0) throw Error(`Invalid discriminated union option at index "${X.options.indexOf(G)}"`);
      for (let B of H) {
        if (J.has(B)) throw Error(`Duplicate discriminator value "${String(B)}"`);
        J.set(B, G);
      }
    }
    return J;
  });
  Q._zod.parse = (W, J) => {
    let G = W.value;
    if (!C6(G)) return W.issues.push({ code: "invalid_type", expected: "object", input: G, inst: Q }), W;
    let H = $.value.get(G?.[X.discriminator]);
    if (H) return H._zod.run(W, J);
    if (X.unionFallback) return Y(W, J);
    return W.issues.push({ code: "invalid_union", errors: [], note: "No matching discriminator", input: G, path: [X.discriminator], inst: Q }), W;
  };
});
var NX = D("$ZodIntersection", (Q, X) => {
  Q0.init(Q, X), Q._zod.parse = (Y, $) => {
    let W = Y.value, J = X.left._zod.run({ value: W, issues: [] }, $), G = X.right._zod.run({ value: W, issues: [] }, $);
    if (J instanceof Promise || G instanceof Promise) return Promise.all([J, G]).then(([B, z]) => {
      return FJ(Y, B, z);
    });
    return FJ(Y, J, G);
  };
});
function cQ(Q, X) {
  if (Q === X) return { valid: true, data: Q };
  if (Q instanceof Date && X instanceof Date && +Q === +X) return { valid: true, data: Q };
  if (k6(Q) && k6(X)) {
    let Y = Object.keys(X), $ = Object.keys(Q).filter((J) => Y.indexOf(J) !== -1), W = { ...Q, ...X };
    for (let J of $) {
      let G = cQ(Q[J], X[J]);
      if (!G.valid) return { valid: false, mergeErrorPath: [J, ...G.mergeErrorPath] };
      W[J] = G.data;
    }
    return { valid: true, data: W };
  }
  if (Array.isArray(Q) && Array.isArray(X)) {
    if (Q.length !== X.length) return { valid: false, mergeErrorPath: [] };
    let Y = [];
    for (let $ = 0; $ < Q.length; $++) {
      let W = Q[$], J = X[$], G = cQ(W, J);
      if (!G.valid) return { valid: false, mergeErrorPath: [$, ...G.mergeErrorPath] };
      Y.push(G.data);
    }
    return { valid: true, data: Y };
  }
  return { valid: false, mergeErrorPath: [] };
}
function FJ(Q, X, Y) {
  if (X.issues.length) Q.issues.push(...X.issues);
  if (Y.issues.length) Q.issues.push(...Y.issues);
  if (X6(Q)) return Q;
  let $ = cQ(X.value, Y.value);
  if (!$.valid) throw Error(`Unmergable intersection. Error path: ${JSON.stringify($.mergeErrorPath)}`);
  return Q.value = $.data, Q;
}
var DX = D("$ZodRecord", (Q, X) => {
  Q0.init(Q, X), Q._zod.parse = (Y, $) => {
    let W = Y.value;
    if (!k6(W)) return Y.issues.push({ expected: "record", code: "invalid_type", input: W, inst: Q }), Y;
    let J = [];
    if (X.keyType._zod.values) {
      let G = X.keyType._zod.values;
      Y.value = {};
      for (let B of G) if (typeof B === "string" || typeof B === "number" || typeof B === "symbol") {
        let z = X.valueType._zod.run({ value: W[B], issues: [] }, $);
        if (z instanceof Promise) J.push(z.then((K) => {
          if (K.issues.length) Y.issues.push(...H1(B, K.issues));
          Y.value[B] = K.value;
        }));
        else {
          if (z.issues.length) Y.issues.push(...H1(B, z.issues));
          Y.value[B] = z.value;
        }
      }
      let H;
      for (let B in W) if (!G.has(B)) H = H ?? [], H.push(B);
      if (H && H.length > 0) Y.issues.push({ code: "unrecognized_keys", input: W, inst: Q, keys: H });
    } else {
      Y.value = {};
      for (let G of Reflect.ownKeys(W)) {
        if (G === "__proto__") continue;
        let H = X.keyType._zod.run({ value: G, issues: [] }, $);
        if (H instanceof Promise) throw Error("Async schemas not supported in object keys currently");
        if (H.issues.length) {
          Y.issues.push({ origin: "record", code: "invalid_key", issues: H.issues.map((z) => r0(z, $, u0())), input: G, path: [G], inst: Q }), Y.value[H.value] = H.value;
          continue;
        }
        let B = X.valueType._zod.run({ value: W[G], issues: [] }, $);
        if (B instanceof Promise) J.push(B.then((z) => {
          if (z.issues.length) Y.issues.push(...H1(G, z.issues));
          Y.value[H.value] = z.value;
        }));
        else {
          if (B.issues.length) Y.issues.push(...H1(G, B.issues));
          Y.value[H.value] = B.value;
        }
      }
    }
    if (J.length) return Promise.all(J).then(() => Y);
    return Y;
  };
});
var OX = D("$ZodEnum", (Q, X) => {
  Q0.init(Q, X);
  let Y = O4(X.entries);
  Q._zod.values = new Set(Y), Q._zod.pattern = new RegExp(`^(${Y.filter(($) => bQ.has(typeof $)).map(($) => typeof $ === "string" ? y1($) : $.toString()).join("|")})$`), Q._zod.parse = ($, W) => {
    let J = $.value;
    if (Q._zod.values.has(J)) return $;
    return $.issues.push({ code: "invalid_value", values: Y, input: J, inst: Q }), $;
  };
});
var wX = D("$ZodLiteral", (Q, X) => {
  Q0.init(Q, X), Q._zod.values = new Set(X.values), Q._zod.pattern = new RegExp(`^(${X.values.map((Y) => typeof Y === "string" ? y1(Y) : Y ? Y.toString() : String(Y)).join("|")})$`), Q._zod.parse = (Y, $) => {
    let W = Y.value;
    if (Q._zod.values.has(W)) return Y;
    return Y.issues.push({ code: "invalid_value", values: X.values, input: W, inst: Q }), Y;
  };
});
var MX = D("$ZodTransform", (Q, X) => {
  Q0.init(Q, X), Q._zod.parse = (Y, $) => {
    let W = X.transform(Y.value, Y);
    if ($.async) return (W instanceof Promise ? W : Promise.resolve(W)).then((G) => {
      return Y.value = G, Y;
    });
    if (W instanceof Promise) throw new x1();
    return Y.value = W, Y;
  };
});
var AX = D("$ZodOptional", (Q, X) => {
  Q0.init(Q, X), Q._zod.optin = "optional", Q._zod.optout = "optional", $0(Q._zod, "values", () => {
    return X.innerType._zod.values ? /* @__PURE__ */ new Set([...X.innerType._zod.values, void 0]) : void 0;
  }), $0(Q._zod, "pattern", () => {
    let Y = X.innerType._zod.pattern;
    return Y ? new RegExp(`^(${A4(Y.source)})?$`) : void 0;
  }), Q._zod.parse = (Y, $) => {
    if (X.innerType._zod.optin === "optional") return X.innerType._zod.run(Y, $);
    if (Y.value === void 0) return Y;
    return X.innerType._zod.run(Y, $);
  };
});
var jX = D("$ZodNullable", (Q, X) => {
  Q0.init(Q, X), $0(Q._zod, "optin", () => X.innerType._zod.optin), $0(Q._zod, "optout", () => X.innerType._zod.optout), $0(Q._zod, "pattern", () => {
    let Y = X.innerType._zod.pattern;
    return Y ? new RegExp(`^(${A4(Y.source)}|null)$`) : void 0;
  }), $0(Q._zod, "values", () => {
    return X.innerType._zod.values ? /* @__PURE__ */ new Set([...X.innerType._zod.values, null]) : void 0;
  }), Q._zod.parse = (Y, $) => {
    if (Y.value === null) return Y;
    return X.innerType._zod.run(Y, $);
  };
});
var RX = D("$ZodDefault", (Q, X) => {
  Q0.init(Q, X), Q._zod.optin = "optional", $0(Q._zod, "values", () => X.innerType._zod.values), Q._zod.parse = (Y, $) => {
    if (Y.value === void 0) return Y.value = X.defaultValue, Y;
    let W = X.innerType._zod.run(Y, $);
    if (W instanceof Promise) return W.then((J) => NJ(J, X));
    return NJ(W, X);
  };
});
function NJ(Q, X) {
  if (Q.value === void 0) Q.value = X.defaultValue;
  return Q;
}
var IX = D("$ZodPrefault", (Q, X) => {
  Q0.init(Q, X), Q._zod.optin = "optional", $0(Q._zod, "values", () => X.innerType._zod.values), Q._zod.parse = (Y, $) => {
    if (Y.value === void 0) Y.value = X.defaultValue;
    return X.innerType._zod.run(Y, $);
  };
});
var EX = D("$ZodNonOptional", (Q, X) => {
  Q0.init(Q, X), $0(Q._zod, "values", () => {
    let Y = X.innerType._zod.values;
    return Y ? new Set([...Y].filter(($) => $ !== void 0)) : void 0;
  }), Q._zod.parse = (Y, $) => {
    let W = X.innerType._zod.run(Y, $);
    if (W instanceof Promise) return W.then((J) => DJ(J, Q));
    return DJ(W, Q);
  };
});
function DJ(Q, X) {
  if (!Q.issues.length && Q.value === void 0) Q.issues.push({ code: "invalid_type", expected: "nonoptional", input: Q.value, inst: X });
  return Q;
}
var bX = D("$ZodCatch", (Q, X) => {
  Q0.init(Q, X), Q._zod.optin = "optional", $0(Q._zod, "optout", () => X.innerType._zod.optout), $0(Q._zod, "values", () => X.innerType._zod.values), Q._zod.parse = (Y, $) => {
    let W = X.innerType._zod.run(Y, $);
    if (W instanceof Promise) return W.then((J) => {
      if (Y.value = J.value, J.issues.length) Y.value = X.catchValue({ ...Y, error: { issues: J.issues.map((G) => r0(G, $, u0())) }, input: Y.value }), Y.issues = [];
      return Y;
    });
    if (Y.value = W.value, W.issues.length) Y.value = X.catchValue({ ...Y, error: { issues: W.issues.map((J) => r0(J, $, u0())) }, input: Y.value }), Y.issues = [];
    return Y;
  };
});
var PX = D("$ZodPipe", (Q, X) => {
  Q0.init(Q, X), $0(Q._zod, "values", () => X.in._zod.values), $0(Q._zod, "optin", () => X.in._zod.optin), $0(Q._zod, "optout", () => X.out._zod.optout), Q._zod.parse = (Y, $) => {
    let W = X.in._zod.run(Y, $);
    if (W instanceof Promise) return W.then((J) => OJ(J, X, $));
    return OJ(W, X, $);
  };
});
function OJ(Q, X, Y) {
  if (X6(Q)) return Q;
  return X.out._zod.run({ value: Q.value, issues: Q.issues }, Y);
}
var SX = D("$ZodReadonly", (Q, X) => {
  Q0.init(Q, X), $0(Q._zod, "propValues", () => X.innerType._zod.propValues), $0(Q._zod, "values", () => X.innerType._zod.values), $0(Q._zod, "optin", () => X.innerType._zod.optin), $0(Q._zod, "optout", () => X.innerType._zod.optout), Q._zod.parse = (Y, $) => {
    let W = X.innerType._zod.run(Y, $);
    if (W instanceof Promise) return W.then(wJ);
    return wJ(W);
  };
});
function wJ(Q) {
  return Q.value = Object.freeze(Q.value), Q;
}
var ZX = D("$ZodCustom", (Q, X) => {
  M0.init(Q, X), Q0.init(Q, X), Q._zod.parse = (Y, $) => {
    return Y;
  }, Q._zod.check = (Y) => {
    let $ = Y.value, W = X.fn($);
    if (W instanceof Promise) return W.then((J) => MJ(J, Y, $, Q));
    MJ(W, Y, $, Q);
    return;
  };
});
function MJ(Q, X, Y, $) {
  if (!Q) {
    let W = { code: "custom", input: Y, inst: $, path: [...$._zod.def.path ?? []], continue: !$._zod.def.abort };
    if ($._zod.def.params) W.params = $._zod.def.params;
    X.issues.push(ZQ(W));
  }
}
var YU = (Q) => {
  let X = typeof Q;
  switch (X) {
    case "number":
      return Number.isNaN(Q) ? "NaN" : "number";
    case "object": {
      if (Array.isArray(Q)) return "array";
      if (Q === null) return "null";
      if (Object.getPrototypeOf(Q) !== Object.prototype && Q.constructor) return Q.constructor.name;
    }
  }
  return X;
};
var $U = () => {
  let Q = { string: { unit: "characters", verb: "to have" }, file: { unit: "bytes", verb: "to have" }, array: { unit: "items", verb: "to have" }, set: { unit: "items", verb: "to have" } };
  function X($) {
    return Q[$] ?? null;
  }
  let Y = { regex: "input", email: "email address", url: "URL", emoji: "emoji", uuid: "UUID", uuidv4: "UUIDv4", uuidv6: "UUIDv6", nanoid: "nanoid", guid: "GUID", cuid: "cuid", cuid2: "cuid2", ulid: "ULID", xid: "XID", ksuid: "KSUID", datetime: "ISO datetime", date: "ISO date", time: "ISO time", duration: "ISO duration", ipv4: "IPv4 address", ipv6: "IPv6 address", cidrv4: "IPv4 range", cidrv6: "IPv6 range", base64: "base64-encoded string", base64url: "base64url-encoded string", json_string: "JSON string", e164: "E.164 number", jwt: "JWT", template_literal: "input" };
  return ($) => {
    switch ($.code) {
      case "invalid_type":
        return `Invalid input: expected ${$.expected}, received ${YU($.input)}`;
      case "invalid_value":
        if ($.values.length === 1) return `Invalid input: expected ${Z9($.values[0])}`;
        return `Invalid option: expected one of ${P9($.values, "|")}`;
      case "too_big": {
        let W = $.inclusive ? "<=" : "<", J = X($.origin);
        if (J) return `Too big: expected ${$.origin ?? "value"} to have ${W}${$.maximum.toString()} ${J.unit ?? "elements"}`;
        return `Too big: expected ${$.origin ?? "value"} to be ${W}${$.maximum.toString()}`;
      }
      case "too_small": {
        let W = $.inclusive ? ">=" : ">", J = X($.origin);
        if (J) return `Too small: expected ${$.origin} to have ${W}${$.minimum.toString()} ${J.unit}`;
        return `Too small: expected ${$.origin} to be ${W}${$.minimum.toString()}`;
      }
      case "invalid_format": {
        let W = $;
        if (W.format === "starts_with") return `Invalid string: must start with "${W.prefix}"`;
        if (W.format === "ends_with") return `Invalid string: must end with "${W.suffix}"`;
        if (W.format === "includes") return `Invalid string: must include "${W.includes}"`;
        if (W.format === "regex") return `Invalid string: must match pattern ${W.pattern}`;
        return `Invalid ${Y[W.format] ?? $.format}`;
      }
      case "not_multiple_of":
        return `Invalid number: must be a multiple of ${$.divisor}`;
      case "unrecognized_keys":
        return `Unrecognized key${$.keys.length > 1 ? "s" : ""}: ${P9($.keys, ", ")}`;
      case "invalid_key":
        return `Invalid key in ${$.origin}`;
      case "invalid_union":
        return "Invalid input";
      case "invalid_element":
        return `Invalid value in ${$.origin}`;
      default:
        return "Invalid input";
    }
  };
};
function CX() {
  return { localeError: $U() };
}
var WU = Symbol("ZodOutput");
var JU = Symbol("ZodInput");
var y9 = class {
  constructor() {
    this._map = /* @__PURE__ */ new WeakMap(), this._idmap = /* @__PURE__ */ new Map();
  }
  add(Q, ...X) {
    let Y = X[0];
    if (this._map.set(Q, Y), Y && typeof Y === "object" && "id" in Y) {
      if (this._idmap.has(Y.id)) throw Error(`ID ${Y.id} already exists in the registry`);
      this._idmap.set(Y.id, Q);
    }
    return this;
  }
  remove(Q) {
    return this._map.delete(Q), this;
  }
  get(Q) {
    let X = Q._zod.parent;
    if (X) {
      let Y = { ...this.get(X) ?? {} };
      return delete Y.id, { ...Y, ...this._map.get(Q) };
    }
    return this._map.get(Q);
  }
  has(Q) {
    return this._map.has(Q);
  }
};
function bJ() {
  return new y9();
}
var g1 = bJ();
function kX(Q, X) {
  return new Q({ type: "string", ...y(X) });
}
function vX(Q, X) {
  return new Q({ type: "string", format: "email", check: "string_format", abort: false, ...y(X) });
}
function g9(Q, X) {
  return new Q({ type: "string", format: "guid", check: "string_format", abort: false, ...y(X) });
}
function _X(Q, X) {
  return new Q({ type: "string", format: "uuid", check: "string_format", abort: false, ...y(X) });
}
function TX(Q, X) {
  return new Q({ type: "string", format: "uuid", check: "string_format", abort: false, version: "v4", ...y(X) });
}
function xX(Q, X) {
  return new Q({ type: "string", format: "uuid", check: "string_format", abort: false, version: "v6", ...y(X) });
}
function yX(Q, X) {
  return new Q({ type: "string", format: "uuid", check: "string_format", abort: false, version: "v7", ...y(X) });
}
function gX(Q, X) {
  return new Q({ type: "string", format: "url", check: "string_format", abort: false, ...y(X) });
}
function hX(Q, X) {
  return new Q({ type: "string", format: "emoji", check: "string_format", abort: false, ...y(X) });
}
function fX(Q, X) {
  return new Q({ type: "string", format: "nanoid", check: "string_format", abort: false, ...y(X) });
}
function uX(Q, X) {
  return new Q({ type: "string", format: "cuid", check: "string_format", abort: false, ...y(X) });
}
function lX(Q, X) {
  return new Q({ type: "string", format: "cuid2", check: "string_format", abort: false, ...y(X) });
}
function mX(Q, X) {
  return new Q({ type: "string", format: "ulid", check: "string_format", abort: false, ...y(X) });
}
function cX(Q, X) {
  return new Q({ type: "string", format: "xid", check: "string_format", abort: false, ...y(X) });
}
function pX(Q, X) {
  return new Q({ type: "string", format: "ksuid", check: "string_format", abort: false, ...y(X) });
}
function dX(Q, X) {
  return new Q({ type: "string", format: "ipv4", check: "string_format", abort: false, ...y(X) });
}
function iX(Q, X) {
  return new Q({ type: "string", format: "ipv6", check: "string_format", abort: false, ...y(X) });
}
function nX(Q, X) {
  return new Q({ type: "string", format: "cidrv4", check: "string_format", abort: false, ...y(X) });
}
function rX(Q, X) {
  return new Q({ type: "string", format: "cidrv6", check: "string_format", abort: false, ...y(X) });
}
function oX(Q, X) {
  return new Q({ type: "string", format: "base64", check: "string_format", abort: false, ...y(X) });
}
function tX(Q, X) {
  return new Q({ type: "string", format: "base64url", check: "string_format", abort: false, ...y(X) });
}
function aX(Q, X) {
  return new Q({ type: "string", format: "e164", check: "string_format", abort: false, ...y(X) });
}
function sX(Q, X) {
  return new Q({ type: "string", format: "jwt", check: "string_format", abort: false, ...y(X) });
}
function PJ(Q, X) {
  return new Q({ type: "string", format: "datetime", check: "string_format", offset: false, local: false, precision: null, ...y(X) });
}
function SJ(Q, X) {
  return new Q({ type: "string", format: "date", check: "string_format", ...y(X) });
}
function ZJ(Q, X) {
  return new Q({ type: "string", format: "time", check: "string_format", precision: null, ...y(X) });
}
function CJ(Q, X) {
  return new Q({ type: "string", format: "duration", check: "string_format", ...y(X) });
}
function eX(Q, X) {
  return new Q({ type: "number", checks: [], ...y(X) });
}
function QY(Q, X) {
  return new Q({ type: "number", check: "number_format", abort: false, format: "safeint", ...y(X) });
}
function XY(Q, X) {
  return new Q({ type: "boolean", ...y(X) });
}
function YY(Q, X) {
  return new Q({ type: "null", ...y(X) });
}
function $Y(Q) {
  return new Q({ type: "unknown" });
}
function WY(Q, X) {
  return new Q({ type: "never", ...y(X) });
}
function h9(Q, X) {
  return new uQ({ check: "less_than", ...y(X), value: Q, inclusive: false });
}
function b4(Q, X) {
  return new uQ({ check: "less_than", ...y(X), value: Q, inclusive: true });
}
function f9(Q, X) {
  return new lQ({ check: "greater_than", ...y(X), value: Q, inclusive: false });
}
function P4(Q, X) {
  return new lQ({ check: "greater_than", ...y(X), value: Q, inclusive: true });
}
function u9(Q, X) {
  return new sW({ check: "multiple_of", ...y(X), value: Q });
}
function l9(Q, X) {
  return new QJ({ check: "max_length", ...y(X), maximum: Q });
}
function v6(Q, X) {
  return new XJ({ check: "min_length", ...y(X), minimum: Q });
}
function m9(Q, X) {
  return new YJ({ check: "length_equals", ...y(X), length: Q });
}
function JY(Q, X) {
  return new $J({ check: "string_format", format: "regex", ...y(X), pattern: Q });
}
function GY(Q) {
  return new WJ({ check: "string_format", format: "lowercase", ...y(Q) });
}
function HY(Q) {
  return new JJ({ check: "string_format", format: "uppercase", ...y(Q) });
}
function BY(Q, X) {
  return new GJ({ check: "string_format", format: "includes", ...y(X), includes: Q });
}
function zY(Q, X) {
  return new HJ({ check: "string_format", format: "starts_with", ...y(X), prefix: Q });
}
function KY(Q, X) {
  return new BJ({ check: "string_format", format: "ends_with", ...y(X), suffix: Q });
}
function W6(Q) {
  return new zJ({ check: "overwrite", tx: Q });
}
function VY(Q) {
  return W6((X) => X.normalize(Q));
}
function qY() {
  return W6((Q) => Q.trim());
}
function UY() {
  return W6((Q) => Q.toLowerCase());
}
function LY() {
  return W6((Q) => Q.toUpperCase());
}
function kJ(Q, X, Y) {
  return new Q({ type: "array", element: X, ...y(Y) });
}
function FY(Q, X, Y) {
  let $ = y(Y);
  return $.abort ?? ($.abort = true), new Q({ type: "custom", check: "custom", fn: X, ...$ });
}
function NY(Q, X, Y) {
  return new Q({ type: "custom", check: "custom", fn: X, ...y(Y) });
}
var uU = D("ZodMiniType", (Q, X) => {
  if (!Q._zod) throw Error("Uninitialized schema in ZodMiniType.");
  Q0.init(Q, X), Q.def = X, Q.parse = (Y, $) => _Q(Q, Y, $, { callee: Q.parse }), Q.safeParse = (Y, $) => Y6(Q, Y, $), Q.parseAsync = async (Y, $) => xQ(Q, Y, $, { callee: Q.parseAsync }), Q.safeParseAsync = async (Y, $) => $6(Q, Y, $), Q.check = (...Y) => {
    return Q.clone({ ...X, checks: [...X.checks ?? [], ...Y.map(($) => typeof $ === "function" ? { _zod: { check: $, def: { check: "custom" }, onattach: [] } } : $)] });
  }, Q.clone = (Y, $) => l0(Q, Y, $), Q.brand = () => Q, Q.register = (Y, $) => {
    return Y.add(Q, $), Q;
  };
});
var lU = D("ZodMiniObject", (Q, X) => {
  T9.init(Q, X), uU.init(Q, X), i.defineLazy(Q, "shape", () => X.shape);
});
var S4 = {};
U7(S4, { time: () => jY, duration: () => RY, datetime: () => MY, date: () => AY, ZodISOTime: () => gJ, ZodISODuration: () => hJ, ZodISODateTime: () => xJ, ZodISODate: () => yJ });
var xJ = D("ZodISODateTime", (Q, X) => {
  AJ.init(Q, X), H0.init(Q, X);
});
function MY(Q) {
  return PJ(xJ, Q);
}
var yJ = D("ZodISODate", (Q, X) => {
  jJ.init(Q, X), H0.init(Q, X);
});
function AY(Q) {
  return SJ(yJ, Q);
}
var gJ = D("ZodISOTime", (Q, X) => {
  RJ.init(Q, X), H0.init(Q, X);
});
function jY(Q) {
  return ZJ(gJ, Q);
}
var hJ = D("ZodISODuration", (Q, X) => {
  IJ.init(Q, X), H0.init(Q, X);
});
function RY(Q) {
  return CJ(hJ, Q);
}
var fJ = (Q, X) => {
  C9.init(Q, X), Q.name = "ZodError", Object.defineProperties(Q, { format: { value: (Y) => kQ(Q, Y) }, flatten: { value: (Y) => CQ(Q, Y) }, addIssue: { value: (Y) => Q.issues.push(Y) }, addIssues: { value: (Y) => Q.issues.push(...Y) }, isEmpty: { get() {
    return Q.issues.length === 0;
  } } });
};
var BZ = D("ZodError", fJ);
var Z4 = D("ZodError", fJ, { Parent: Error });
var uJ = vQ(Z4);
var lJ = TQ(Z4);
var mJ = yQ(Z4);
var cJ = gQ(Z4);
var z0 = D("ZodType", (Q, X) => {
  return Q0.init(Q, X), Q.def = X, Object.defineProperty(Q, "_def", { value: X }), Q.check = (...Y) => {
    return Q.clone({ ...X, checks: [...X.checks ?? [], ...Y.map(($) => typeof $ === "function" ? { _zod: { check: $, def: { check: "custom" }, onattach: [] } } : $)] });
  }, Q.clone = (Y, $) => l0(Q, Y, $), Q.brand = () => Q, Q.register = (Y, $) => {
    return Y.add(Q, $), Q;
  }, Q.parse = (Y, $) => uJ(Q, Y, $, { callee: Q.parse }), Q.safeParse = (Y, $) => mJ(Q, Y, $), Q.parseAsync = async (Y, $) => lJ(Q, Y, $, { callee: Q.parseAsync }), Q.safeParseAsync = async (Y, $) => cJ(Q, Y, $), Q.spa = Q.safeParseAsync, Q.refine = (Y, $) => Q.check(hL(Y, $)), Q.superRefine = (Y) => Q.check(fL(Y)), Q.overwrite = (Y) => Q.check(W6(Y)), Q.optional = () => v(Q), Q.nullable = () => iJ(Q), Q.nullish = () => v(iJ(Q)), Q.nonoptional = (Y) => kL(Q, Y), Q.array = () => r(Q), Q.or = (Y) => J0([Q, Y]), Q.and = (Y) => n9(Q, Y), Q.transform = (Y) => EY(Q, aJ(Y)), Q.default = (Y) => SL(Q, Y), Q.prefault = (Y) => CL(Q, Y), Q.catch = (Y) => _L(Q, Y), Q.pipe = (Y) => EY(Q, Y), Q.readonly = () => yL(Q), Q.describe = (Y) => {
    let $ = Q.clone();
    return g1.add($, { description: Y }), $;
  }, Object.defineProperty(Q, "description", { get() {
    return g1.get(Q)?.description;
  }, configurable: true }), Q.meta = (...Y) => {
    if (Y.length === 0) return g1.get(Q);
    let $ = Q.clone();
    return g1.add($, Y[0]), $;
  }, Q.isOptional = () => Q.safeParse(void 0).success, Q.isNullable = () => Q.safeParse(null).success, Q;
});
var nJ = D("_ZodString", (Q, X) => {
  E4.init(Q, X), z0.init(Q, X);
  let Y = Q._zod.bag;
  Q.format = Y.format ?? null, Q.minLength = Y.minimum ?? null, Q.maxLength = Y.maximum ?? null, Q.regex = (...$) => Q.check(JY(...$)), Q.includes = (...$) => Q.check(BY(...$)), Q.startsWith = (...$) => Q.check(zY(...$)), Q.endsWith = (...$) => Q.check(KY(...$)), Q.min = (...$) => Q.check(v6(...$)), Q.max = (...$) => Q.check(l9(...$)), Q.length = (...$) => Q.check(m9(...$)), Q.nonempty = (...$) => Q.check(v6(1, ...$)), Q.lowercase = ($) => Q.check(GY($)), Q.uppercase = ($) => Q.check(HY($)), Q.trim = () => Q.check(qY()), Q.normalize = (...$) => Q.check(VY(...$)), Q.toLowerCase = () => Q.check(UY()), Q.toUpperCase = () => Q.check(LY());
});
var tU = D("ZodString", (Q, X) => {
  E4.init(Q, X), nJ.init(Q, X), Q.email = (Y) => Q.check(vX(aU, Y)), Q.url = (Y) => Q.check(gX(sU, Y)), Q.jwt = (Y) => Q.check(sX(UL, Y)), Q.emoji = (Y) => Q.check(hX(eU, Y)), Q.guid = (Y) => Q.check(g9(pJ, Y)), Q.uuid = (Y) => Q.check(_X(i9, Y)), Q.uuidv4 = (Y) => Q.check(TX(i9, Y)), Q.uuidv6 = (Y) => Q.check(xX(i9, Y)), Q.uuidv7 = (Y) => Q.check(yX(i9, Y)), Q.nanoid = (Y) => Q.check(fX(QL, Y)), Q.guid = (Y) => Q.check(g9(pJ, Y)), Q.cuid = (Y) => Q.check(uX(XL, Y)), Q.cuid2 = (Y) => Q.check(lX(YL, Y)), Q.ulid = (Y) => Q.check(mX($L, Y)), Q.base64 = (Y) => Q.check(oX(KL, Y)), Q.base64url = (Y) => Q.check(tX(VL, Y)), Q.xid = (Y) => Q.check(cX(WL, Y)), Q.ksuid = (Y) => Q.check(pX(JL, Y)), Q.ipv4 = (Y) => Q.check(dX(GL, Y)), Q.ipv6 = (Y) => Q.check(iX(HL, Y)), Q.cidrv4 = (Y) => Q.check(nX(BL, Y)), Q.cidrv6 = (Y) => Q.check(rX(zL, Y)), Q.e164 = (Y) => Q.check(aX(qL, Y)), Q.datetime = (Y) => Q.check(MY(Y)), Q.date = (Y) => Q.check(AY(Y)), Q.time = (Y) => Q.check(jY(Y)), Q.duration = (Y) => Q.check(RY(Y));
});
function O(Q) {
  return kX(tU, Q);
}
var H0 = D("ZodStringFormat", (Q, X) => {
  W0.init(Q, X), nJ.init(Q, X);
});
var aU = D("ZodEmail", (Q, X) => {
  iQ.init(Q, X), H0.init(Q, X);
});
var pJ = D("ZodGUID", (Q, X) => {
  pQ.init(Q, X), H0.init(Q, X);
});
var i9 = D("ZodUUID", (Q, X) => {
  dQ.init(Q, X), H0.init(Q, X);
});
var sU = D("ZodURL", (Q, X) => {
  nQ.init(Q, X), H0.init(Q, X);
});
var eU = D("ZodEmoji", (Q, X) => {
  rQ.init(Q, X), H0.init(Q, X);
});
var QL = D("ZodNanoID", (Q, X) => {
  oQ.init(Q, X), H0.init(Q, X);
});
var XL = D("ZodCUID", (Q, X) => {
  tQ.init(Q, X), H0.init(Q, X);
});
var YL = D("ZodCUID2", (Q, X) => {
  aQ.init(Q, X), H0.init(Q, X);
});
var $L = D("ZodULID", (Q, X) => {
  sQ.init(Q, X), H0.init(Q, X);
});
var WL = D("ZodXID", (Q, X) => {
  eQ.init(Q, X), H0.init(Q, X);
});
var JL = D("ZodKSUID", (Q, X) => {
  QX.init(Q, X), H0.init(Q, X);
});
var GL = D("ZodIPv4", (Q, X) => {
  XX.init(Q, X), H0.init(Q, X);
});
var HL = D("ZodIPv6", (Q, X) => {
  YX.init(Q, X), H0.init(Q, X);
});
var BL = D("ZodCIDRv4", (Q, X) => {
  $X.init(Q, X), H0.init(Q, X);
});
var zL = D("ZodCIDRv6", (Q, X) => {
  WX.init(Q, X), H0.init(Q, X);
});
var KL = D("ZodBase64", (Q, X) => {
  JX.init(Q, X), H0.init(Q, X);
});
var VL = D("ZodBase64URL", (Q, X) => {
  GX.init(Q, X), H0.init(Q, X);
});
var qL = D("ZodE164", (Q, X) => {
  HX.init(Q, X), H0.init(Q, X);
});
var UL = D("ZodJWT", (Q, X) => {
  BX.init(Q, X), H0.init(Q, X);
});
var rJ = D("ZodNumber", (Q, X) => {
  _9.init(Q, X), z0.init(Q, X), Q.gt = ($, W) => Q.check(f9($, W)), Q.gte = ($, W) => Q.check(P4($, W)), Q.min = ($, W) => Q.check(P4($, W)), Q.lt = ($, W) => Q.check(h9($, W)), Q.lte = ($, W) => Q.check(b4($, W)), Q.max = ($, W) => Q.check(b4($, W)), Q.int = ($) => Q.check(dJ($)), Q.safe = ($) => Q.check(dJ($)), Q.positive = ($) => Q.check(f9(0, $)), Q.nonnegative = ($) => Q.check(P4(0, $)), Q.negative = ($) => Q.check(h9(0, $)), Q.nonpositive = ($) => Q.check(b4(0, $)), Q.multipleOf = ($, W) => Q.check(u9($, W)), Q.step = ($, W) => Q.check(u9($, W)), Q.finite = () => Q;
  let Y = Q._zod.bag;
  Q.minValue = Math.max(Y.minimum ?? Number.NEGATIVE_INFINITY, Y.exclusiveMinimum ?? Number.NEGATIVE_INFINITY) ?? null, Q.maxValue = Math.min(Y.maximum ?? Number.POSITIVE_INFINITY, Y.exclusiveMaximum ?? Number.POSITIVE_INFINITY) ?? null, Q.isInt = (Y.format ?? "").includes("int") || Number.isSafeInteger(Y.multipleOf ?? 0.5), Q.isFinite = true, Q.format = Y.format ?? null;
});
function X0(Q) {
  return eX(rJ, Q);
}
var LL = D("ZodNumberFormat", (Q, X) => {
  zX.init(Q, X), rJ.init(Q, X);
});
function dJ(Q) {
  return QY(LL, Q);
}
var FL = D("ZodBoolean", (Q, X) => {
  KX.init(Q, X), z0.init(Q, X);
});
function A0(Q) {
  return XY(FL, Q);
}
var NL = D("ZodNull", (Q, X) => {
  VX.init(Q, X), z0.init(Q, X);
});
function bY(Q) {
  return YY(NL, Q);
}
var DL = D("ZodUnknown", (Q, X) => {
  qX.init(Q, X), z0.init(Q, X);
});
function N0() {
  return $Y(DL);
}
var OL = D("ZodNever", (Q, X) => {
  UX.init(Q, X), z0.init(Q, X);
});
function wL(Q) {
  return WY(OL, Q);
}
var ML = D("ZodArray", (Q, X) => {
  LX.init(Q, X), z0.init(Q, X), Q.element = X.element, Q.min = (Y, $) => Q.check(v6(Y, $)), Q.nonempty = (Y) => Q.check(v6(1, Y)), Q.max = (Y, $) => Q.check(l9(Y, $)), Q.length = (Y, $) => Q.check(m9(Y, $)), Q.unwrap = () => Q.element;
});
function r(Q, X) {
  return kJ(ML, Q, X);
}
var oJ = D("ZodObject", (Q, X) => {
  T9.init(Q, X), z0.init(Q, X), i.defineLazy(Q, "shape", () => X.shape), Q.keyof = () => j0(Object.keys(Q._zod.def.shape)), Q.catchall = (Y) => Q.clone({ ...Q._zod.def, catchall: Y }), Q.passthrough = () => Q.clone({ ...Q._zod.def, catchall: N0() }), Q.loose = () => Q.clone({ ...Q._zod.def, catchall: N0() }), Q.strict = () => Q.clone({ ...Q._zod.def, catchall: wL() }), Q.strip = () => Q.clone({ ...Q._zod.def, catchall: void 0 }), Q.extend = (Y) => {
    return i.extend(Q, Y);
  }, Q.merge = (Y) => i.merge(Q, Y), Q.pick = (Y) => i.pick(Q, Y), Q.omit = (Y) => i.omit(Q, Y), Q.partial = (...Y) => i.partial(sJ, Q, Y[0]), Q.required = (...Y) => i.required(eJ, Q, Y[0]);
});
function E(Q, X) {
  let Y = { type: "object", get shape() {
    return i.assignProp(this, "shape", { ...Q }), this.shape;
  }, ...i.normalizeParams(X) };
  return new oJ(Y);
}
function c0(Q, X) {
  return new oJ({ type: "object", get shape() {
    return i.assignProp(this, "shape", { ...Q }), this.shape;
  }, catchall: N0(), ...i.normalizeParams(X) });
}
var tJ = D("ZodUnion", (Q, X) => {
  x9.init(Q, X), z0.init(Q, X), Q.options = X.options;
});
function J0(Q, X) {
  return new tJ({ type: "union", options: Q, ...i.normalizeParams(X) });
}
var AL = D("ZodDiscriminatedUnion", (Q, X) => {
  tJ.init(Q, X), FX.init(Q, X);
});
function PY(Q, X, Y) {
  return new AL({ type: "union", options: X, discriminator: Q, ...i.normalizeParams(Y) });
}
var jL = D("ZodIntersection", (Q, X) => {
  NX.init(Q, X), z0.init(Q, X);
});
function n9(Q, X) {
  return new jL({ type: "intersection", left: Q, right: X });
}
var RL = D("ZodRecord", (Q, X) => {
  DX.init(Q, X), z0.init(Q, X), Q.keyType = X.keyType, Q.valueType = X.valueType;
});
function D0(Q, X, Y) {
  return new RL({ type: "record", keyType: Q, valueType: X, ...i.normalizeParams(Y) });
}
var IY = D("ZodEnum", (Q, X) => {
  OX.init(Q, X), z0.init(Q, X), Q.enum = X.entries, Q.options = Object.values(X.entries);
  let Y = new Set(Object.keys(X.entries));
  Q.extract = ($, W) => {
    let J = {};
    for (let G of $) if (Y.has(G)) J[G] = X.entries[G];
    else throw Error(`Key ${G} not found in enum`);
    return new IY({ ...X, checks: [], ...i.normalizeParams(W), entries: J });
  }, Q.exclude = ($, W) => {
    let J = { ...X.entries };
    for (let G of $) if (Y.has(G)) delete J[G];
    else throw Error(`Key ${G} not found in enum`);
    return new IY({ ...X, checks: [], ...i.normalizeParams(W), entries: J });
  };
});
function j0(Q, X) {
  let Y = Array.isArray(Q) ? Object.fromEntries(Q.map(($) => [$, $])) : Q;
  return new IY({ type: "enum", entries: Y, ...i.normalizeParams(X) });
}
var IL = D("ZodLiteral", (Q, X) => {
  wX.init(Q, X), z0.init(Q, X), Q.values = new Set(X.values), Object.defineProperty(Q, "value", { get() {
    if (X.values.length > 1) throw Error("This schema contains multiple valid literal values. Use `.values` instead.");
    return X.values[0];
  } });
});
function _(Q, X) {
  return new IL({ type: "literal", values: Array.isArray(Q) ? Q : [Q], ...i.normalizeParams(X) });
}
var EL = D("ZodTransform", (Q, X) => {
  MX.init(Q, X), z0.init(Q, X), Q._zod.parse = (Y, $) => {
    Y.addIssue = (J) => {
      if (typeof J === "string") Y.issues.push(i.issue(J, Y.value, X));
      else {
        let G = J;
        if (G.fatal) G.continue = false;
        G.code ?? (G.code = "custom"), G.input ?? (G.input = Y.value), G.inst ?? (G.inst = Q), G.continue ?? (G.continue = true), Y.issues.push(i.issue(G));
      }
    };
    let W = X.transform(Y.value, Y);
    if (W instanceof Promise) return W.then((J) => {
      return Y.value = J, Y;
    });
    return Y.value = W, Y;
  };
});
function aJ(Q) {
  return new EL({ type: "transform", transform: Q });
}
var sJ = D("ZodOptional", (Q, X) => {
  AX.init(Q, X), z0.init(Q, X), Q.unwrap = () => Q._zod.def.innerType;
});
function v(Q) {
  return new sJ({ type: "optional", innerType: Q });
}
var bL = D("ZodNullable", (Q, X) => {
  jX.init(Q, X), z0.init(Q, X), Q.unwrap = () => Q._zod.def.innerType;
});
function iJ(Q) {
  return new bL({ type: "nullable", innerType: Q });
}
var PL = D("ZodDefault", (Q, X) => {
  RX.init(Q, X), z0.init(Q, X), Q.unwrap = () => Q._zod.def.innerType, Q.removeDefault = Q.unwrap;
});
function SL(Q, X) {
  return new PL({ type: "default", innerType: Q, get defaultValue() {
    return typeof X === "function" ? X() : X;
  } });
}
var ZL = D("ZodPrefault", (Q, X) => {
  IX.init(Q, X), z0.init(Q, X), Q.unwrap = () => Q._zod.def.innerType;
});
function CL(Q, X) {
  return new ZL({ type: "prefault", innerType: Q, get defaultValue() {
    return typeof X === "function" ? X() : X;
  } });
}
var eJ = D("ZodNonOptional", (Q, X) => {
  EX.init(Q, X), z0.init(Q, X), Q.unwrap = () => Q._zod.def.innerType;
});
function kL(Q, X) {
  return new eJ({ type: "nonoptional", innerType: Q, ...i.normalizeParams(X) });
}
var vL = D("ZodCatch", (Q, X) => {
  bX.init(Q, X), z0.init(Q, X), Q.unwrap = () => Q._zod.def.innerType, Q.removeCatch = Q.unwrap;
});
function _L(Q, X) {
  return new vL({ type: "catch", innerType: Q, catchValue: typeof X === "function" ? X : () => X });
}
var TL = D("ZodPipe", (Q, X) => {
  PX.init(Q, X), z0.init(Q, X), Q.in = X.in, Q.out = X.out;
});
function EY(Q, X) {
  return new TL({ type: "pipe", in: Q, out: X });
}
var xL = D("ZodReadonly", (Q, X) => {
  SX.init(Q, X), z0.init(Q, X);
});
function yL(Q) {
  return new xL({ type: "readonly", innerType: Q });
}
var Q5 = D("ZodCustom", (Q, X) => {
  ZX.init(Q, X), z0.init(Q, X);
});
function gL(Q, X) {
  let Y = new M0({ check: "custom", ...i.normalizeParams(X) });
  return Y._zod.check = Q, Y;
}
function X5(Q, X) {
  return FY(Q5, Q ?? (() => true), X);
}
function hL(Q, X = {}) {
  return NY(Q5, Q, X);
}
function fL(Q, X) {
  let Y = gL(($) => {
    return $.addIssue = (W) => {
      if (typeof W === "string") $.issues.push(i.issue(W, $.value, Y._zod.def));
      else {
        let J = W;
        if (J.fatal) J.continue = false;
        J.code ?? (J.code = "custom"), J.input ?? (J.input = $.value), J.inst ?? (J.inst = Y), J.continue ?? (J.continue = !Y._zod.def.abort), $.issues.push(i.issue(J));
      }
    }, Q($.value, $);
  }, X);
  return Y;
}
function SY(Q, X) {
  return EY(aJ(Q), X);
}
u0(CX());
var z1 = "io.modelcontextprotocol/related-task";
var o9 = "2.0";
var B1 = X5((Q) => Q !== null && (typeof Q === "object" || typeof Q === "function"));
var $5 = J0([O(), X0().int()]);
var W5 = O();
var uL = c0({ ttl: J0([X0(), bY()]).optional(), pollInterval: X0().optional() });
var CY = c0({ taskId: O() });
var lL = c0({ progressToken: $5.optional(), [z1]: CY.optional() });
var T0 = c0({ task: uL.optional(), _meta: lL.optional() });
var R0 = E({ method: O(), params: T0.optional() });
var G6 = c0({ _meta: E({ [z1]: v(CY) }).passthrough().optional() });
var p0 = E({ method: O(), params: G6.optional() });
var P0 = c0({ _meta: c0({ [z1]: CY.optional() }).optional() });
var t9 = J0([O(), X0().int()]);
var J5 = E({ jsonrpc: _(o9), id: t9, ...R0.shape }).strict();
var G5 = E({ jsonrpc: _(o9), ...p0.shape }).strict();
var B5 = E({ jsonrpc: _(o9), id: t9, result: P0 }).strict();
var x;
(function(Q) {
  Q[Q.ConnectionClosed = -32e3] = "ConnectionClosed", Q[Q.RequestTimeout = -32001] = "RequestTimeout", Q[Q.ParseError = -32700] = "ParseError", Q[Q.InvalidRequest = -32600] = "InvalidRequest", Q[Q.MethodNotFound = -32601] = "MethodNotFound", Q[Q.InvalidParams = -32602] = "InvalidParams", Q[Q.InternalError = -32603] = "InternalError", Q[Q.UrlElicitationRequired = -32042] = "UrlElicitationRequired";
})(x || (x = {}));
var z5 = E({ jsonrpc: _(o9), id: t9, error: E({ code: X0().int(), message: O(), data: v(N0()) }) }).strict();
var jZ = J0([J5, G5, B5, z5]);
var a9 = P0.strict();
var mL = G6.extend({ requestId: t9, reason: O().optional() });
var s9 = p0.extend({ method: _("notifications/cancelled"), params: mL });
var cL = E({ src: O(), mimeType: O().optional(), sizes: r(O()).optional() });
var k4 = E({ icons: r(cL).optional() });
var x6 = E({ name: O(), title: O().optional() });
var V5 = x6.extend({ ...x6.shape, ...k4.shape, version: O(), websiteUrl: O().optional() });
var pL = n9(E({ applyDefaults: A0().optional() }), D0(O(), N0()));
var dL = SY((Q) => {
  if (Q && typeof Q === "object" && !Array.isArray(Q)) {
    if (Object.keys(Q).length === 0) return { form: {} };
  }
  return Q;
}, n9(E({ form: pL.optional(), url: B1.optional() }), D0(O(), N0()).optional()));
var iL = E({ list: v(E({}).passthrough()), cancel: v(E({}).passthrough()), requests: v(E({ sampling: v(E({ createMessage: v(E({}).passthrough()) }).passthrough()), elicitation: v(E({ create: v(E({}).passthrough()) }).passthrough()) }).passthrough()) }).passthrough();
var nL = E({ list: v(E({}).passthrough()), cancel: v(E({}).passthrough()), requests: v(E({ tools: v(E({ call: v(E({}).passthrough()) }).passthrough()) }).passthrough()) }).passthrough();
var rL = E({ experimental: D0(O(), B1).optional(), sampling: E({ context: B1.optional(), tools: B1.optional() }).optional(), elicitation: dL.optional(), roots: E({ listChanged: A0().optional() }).optional(), tasks: v(iL) });
var oL = T0.extend({ protocolVersion: O(), capabilities: rL, clientInfo: V5 });
var vY = R0.extend({ method: _("initialize"), params: oL });
var tL = E({ experimental: D0(O(), B1).optional(), logging: B1.optional(), completions: B1.optional(), prompts: v(E({ listChanged: v(A0()) })), resources: E({ subscribe: A0().optional(), listChanged: A0().optional() }).optional(), tools: E({ listChanged: A0().optional() }).optional(), tasks: v(nL) }).passthrough();
var aL = P0.extend({ protocolVersion: O(), capabilities: tL, serverInfo: V5, instructions: O().optional() });
var _Y = p0.extend({ method: _("notifications/initialized") });
var e9 = R0.extend({ method: _("ping") });
var sL = E({ progress: X0(), total: v(X0()), message: v(O()) });
var eL = E({ ...G6.shape, ...sL.shape, progressToken: $5 });
var Q8 = p0.extend({ method: _("notifications/progress"), params: eL });
var QF = T0.extend({ cursor: W5.optional() });
var v4 = R0.extend({ params: QF.optional() });
var _4 = P0.extend({ nextCursor: v(W5) });
var T4 = E({ taskId: O(), status: j0(["working", "input_required", "completed", "failed", "cancelled"]), ttl: J0([X0(), bY()]), createdAt: O(), lastUpdatedAt: O(), pollInterval: v(X0()), statusMessage: v(O()) });
var y6 = P0.extend({ task: T4 });
var XF = G6.merge(T4);
var x4 = p0.extend({ method: _("notifications/tasks/status"), params: XF });
var X8 = R0.extend({ method: _("tasks/get"), params: T0.extend({ taskId: O() }) });
var Y8 = P0.merge(T4);
var $8 = R0.extend({ method: _("tasks/result"), params: T0.extend({ taskId: O() }) });
var W8 = v4.extend({ method: _("tasks/list") });
var J8 = _4.extend({ tasks: r(T4) });
var q5 = R0.extend({ method: _("tasks/cancel"), params: T0.extend({ taskId: O() }) });
var U5 = P0.merge(T4);
var L5 = E({ uri: O(), mimeType: v(O()), _meta: D0(O(), N0()).optional() });
var F5 = L5.extend({ text: O() });
var TY = O().refine((Q) => {
  try {
    return atob(Q), true;
  } catch (X) {
    return false;
  }
}, { message: "Invalid Base64 string" });
var N5 = L5.extend({ blob: TY });
var g6 = E({ audience: r(j0(["user", "assistant"])).optional(), priority: X0().min(0).max(1).optional(), lastModified: S4.datetime({ offset: true }).optional() });
var D5 = E({ ...x6.shape, ...k4.shape, uri: O(), description: v(O()), mimeType: v(O()), annotations: g6.optional(), _meta: v(c0({})) });
var YF = E({ ...x6.shape, ...k4.shape, uriTemplate: O(), description: v(O()), mimeType: v(O()), annotations: g6.optional(), _meta: v(c0({})) });
var G8 = v4.extend({ method: _("resources/list") });
var $F = _4.extend({ resources: r(D5) });
var H8 = v4.extend({ method: _("resources/templates/list") });
var WF = _4.extend({ resourceTemplates: r(YF) });
var xY = T0.extend({ uri: O() });
var JF = xY;
var B8 = R0.extend({ method: _("resources/read"), params: JF });
var GF = P0.extend({ contents: r(J0([F5, N5])) });
var HF = p0.extend({ method: _("notifications/resources/list_changed") });
var BF = xY;
var zF = R0.extend({ method: _("resources/subscribe"), params: BF });
var KF = xY;
var VF = R0.extend({ method: _("resources/unsubscribe"), params: KF });
var qF = G6.extend({ uri: O() });
var UF = p0.extend({ method: _("notifications/resources/updated"), params: qF });
var LF = E({ name: O(), description: v(O()), required: v(A0()) });
var FF = E({ ...x6.shape, ...k4.shape, description: v(O()), arguments: v(r(LF)), _meta: v(c0({})) });
var z8 = v4.extend({ method: _("prompts/list") });
var NF = _4.extend({ prompts: r(FF) });
var DF = T0.extend({ name: O(), arguments: D0(O(), O()).optional() });
var K8 = R0.extend({ method: _("prompts/get"), params: DF });
var yY = E({ type: _("text"), text: O(), annotations: g6.optional(), _meta: D0(O(), N0()).optional() });
var gY = E({ type: _("image"), data: TY, mimeType: O(), annotations: g6.optional(), _meta: D0(O(), N0()).optional() });
var hY = E({ type: _("audio"), data: TY, mimeType: O(), annotations: g6.optional(), _meta: D0(O(), N0()).optional() });
var OF = E({ type: _("tool_use"), name: O(), id: O(), input: E({}).passthrough(), _meta: v(E({}).passthrough()) }).passthrough();
var wF = E({ type: _("resource"), resource: J0([F5, N5]), annotations: g6.optional(), _meta: D0(O(), N0()).optional() });
var MF = D5.extend({ type: _("resource_link") });
var fY = J0([yY, gY, hY, MF, wF]);
var AF = E({ role: j0(["user", "assistant"]), content: fY });
var jF = P0.extend({ description: v(O()), messages: r(AF) });
var RF = p0.extend({ method: _("notifications/prompts/list_changed") });
var IF = E({ title: O().optional(), readOnlyHint: A0().optional(), destructiveHint: A0().optional(), idempotentHint: A0().optional(), openWorldHint: A0().optional() });
var EF = E({ taskSupport: j0(["required", "optional", "forbidden"]).optional() });
var O5 = E({ ...x6.shape, ...k4.shape, description: O().optional(), inputSchema: E({ type: _("object"), properties: D0(O(), B1).optional(), required: r(O()).optional() }).catchall(N0()), outputSchema: E({ type: _("object"), properties: D0(O(), B1).optional(), required: r(O()).optional() }).catchall(N0()).optional(), annotations: v(IF), execution: v(EF), _meta: D0(O(), N0()).optional() });
var V8 = v4.extend({ method: _("tools/list") });
var bF = _4.extend({ tools: r(O5) });
var q8 = P0.extend({ content: r(fY).default([]), structuredContent: D0(O(), N0()).optional(), isError: v(A0()) });
var RZ = q8.or(P0.extend({ toolResult: N0() }));
var PF = T0.extend({ name: O(), arguments: v(D0(O(), N0())) });
var h6 = R0.extend({ method: _("tools/call"), params: PF });
var SF = p0.extend({ method: _("notifications/tools/list_changed") });
var y4 = j0(["debug", "info", "notice", "warning", "error", "critical", "alert", "emergency"]);
var ZF = T0.extend({ level: y4 });
var uY = R0.extend({ method: _("logging/setLevel"), params: ZF });
var CF = G6.extend({ level: y4, logger: O().optional(), data: N0() });
var kF = p0.extend({ method: _("notifications/message"), params: CF });
var vF = E({ name: O().optional() });
var _F = E({ hints: v(r(vF)), costPriority: v(X0().min(0).max(1)), speedPriority: v(X0().min(0).max(1)), intelligencePriority: v(X0().min(0).max(1)) });
var TF = E({ mode: v(j0(["auto", "required", "none"])) });
var xF = E({ type: _("tool_result"), toolUseId: O().describe("The unique identifier for the corresponding tool call."), content: r(fY).default([]), structuredContent: E({}).passthrough().optional(), isError: v(A0()), _meta: v(E({}).passthrough()) }).passthrough();
var yF = PY("type", [yY, gY, hY]);
var r9 = PY("type", [yY, gY, hY, OF, xF]);
var gF = E({ role: j0(["user", "assistant"]), content: J0([r9, r(r9)]), _meta: v(E({}).passthrough()) }).passthrough();
var hF = T0.extend({ messages: r(gF), modelPreferences: _F.optional(), systemPrompt: O().optional(), includeContext: j0(["none", "thisServer", "allServers"]).optional(), temperature: X0().optional(), maxTokens: X0().int(), stopSequences: r(O()).optional(), metadata: B1.optional(), tools: v(r(O5)), toolChoice: v(TF) });
var fF = R0.extend({ method: _("sampling/createMessage"), params: hF });
var lY = P0.extend({ model: O(), stopReason: v(j0(["endTurn", "stopSequence", "maxTokens"]).or(O())), role: j0(["user", "assistant"]), content: yF });
var mY = P0.extend({ model: O(), stopReason: v(j0(["endTurn", "stopSequence", "maxTokens", "toolUse"]).or(O())), role: j0(["user", "assistant"]), content: J0([r9, r(r9)]) });
var uF = E({ type: _("boolean"), title: O().optional(), description: O().optional(), default: A0().optional() });
var lF = E({ type: _("string"), title: O().optional(), description: O().optional(), minLength: X0().optional(), maxLength: X0().optional(), format: j0(["email", "uri", "date", "date-time"]).optional(), default: O().optional() });
var mF = E({ type: j0(["number", "integer"]), title: O().optional(), description: O().optional(), minimum: X0().optional(), maximum: X0().optional(), default: X0().optional() });
var cF = E({ type: _("string"), title: O().optional(), description: O().optional(), enum: r(O()), default: O().optional() });
var pF = E({ type: _("string"), title: O().optional(), description: O().optional(), oneOf: r(E({ const: O(), title: O() })), default: O().optional() });
var dF = E({ type: _("string"), title: O().optional(), description: O().optional(), enum: r(O()), enumNames: r(O()).optional(), default: O().optional() });
var iF = J0([cF, pF]);
var nF = E({ type: _("array"), title: O().optional(), description: O().optional(), minItems: X0().optional(), maxItems: X0().optional(), items: E({ type: _("string"), enum: r(O()) }), default: r(O()).optional() });
var rF = E({ type: _("array"), title: O().optional(), description: O().optional(), minItems: X0().optional(), maxItems: X0().optional(), items: E({ anyOf: r(E({ const: O(), title: O() })) }), default: r(O()).optional() });
var oF = J0([nF, rF]);
var tF = J0([dF, iF, oF]);
var aF = J0([tF, uF, lF, mF]);
var sF = T0.extend({ mode: _("form").optional(), message: O(), requestedSchema: E({ type: _("object"), properties: D0(O(), aF), required: r(O()).optional() }) });
var eF = T0.extend({ mode: _("url"), message: O(), elicitationId: O(), url: O().url() });
var QN = J0([sF, eF]);
var XN = R0.extend({ method: _("elicitation/create"), params: QN });
var YN = G6.extend({ elicitationId: O() });
var $N = p0.extend({ method: _("notifications/elicitation/complete"), params: YN });
var U8 = P0.extend({ action: j0(["accept", "decline", "cancel"]), content: SY((Q) => Q === null ? void 0 : Q, D0(O(), J0([O(), X0(), A0(), r(O())])).optional()) });
var WN = E({ type: _("ref/resource"), uri: O() });
var JN = E({ type: _("ref/prompt"), name: O() });
var GN = T0.extend({ ref: J0([JN, WN]), argument: E({ name: O(), value: O() }), context: E({ arguments: D0(O(), O()).optional() }).optional() });
var L8 = R0.extend({ method: _("completion/complete"), params: GN });
var HN = P0.extend({ completion: c0({ values: r(O()).max(100), total: v(X0().int()), hasMore: v(A0()) }) });
var BN = E({ uri: O().startsWith("file://"), name: O().optional(), _meta: D0(O(), N0()).optional() });
var zN = R0.extend({ method: _("roots/list") });
var cY = P0.extend({ roots: r(BN) });
var KN = p0.extend({ method: _("notifications/roots/list_changed") });
var IZ = J0([e9, vY, L8, uY, K8, z8, G8, H8, B8, zF, VF, h6, V8, X8, $8, W8]);
var EZ = J0([s9, Q8, _Y, KN, x4]);
var bZ = J0([a9, lY, mY, U8, cY, Y8, J8, y6]);
var PZ = J0([e9, fF, XN, zN, X8, $8, W8]);
var SZ = J0([s9, Q8, kF, UF, HF, SF, RF, x4, $N]);
var ZZ = J0([a9, aL, HN, jF, NF, $F, WF, GF, q8, bF, Y8, J8, y6]);
var R5 = Symbol("Let zodToJsonSchema decide on which parser to use");
var UN = new Set("ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvxyz0123456789");
var hz = q7(d$(), 1);
var fz = q7(gz(), 1);
var cz = Symbol.for("mcp.completable");
var mz;
(function(Q) {
  Q.Completable = "McpCompletable";
})(mz || (mz = {}));
function eT({ prompt: Q, options: X }) {
  let { systemPrompt: Y, settingSources: $, sandbox: W, ...J } = X ?? {}, G, H;
  if (Y === void 0) G = "";
  else if (typeof Y === "string") G = Y;
  else if (Y.type === "preset") H = Y.append;
  let B = J.pathToClaudeCodeExecutable;
  if (!B) {
    let F6 = gI(import.meta.url), N6 = oz(F6, "..");
    B = oz(N6, "cli.js");
  }
  process.env.CLAUDE_AGENT_SDK_VERSION = "0.2.45";
  let { abortController: z = D6(), additionalDirectories: K = [], agent: q, agents: U, allowedTools: V = [], betas: F, canUseTool: L, continue: N, cwd: w, debug: A, debugFile: R, disallowedTools: S = [], tools: C, env: K0, executable: V0 = R6() ? "bun" : "node", executableArgs: s = [], extraArgs: O0 = {}, fallbackModel: L0, enableFileCheckpointing: U1, forkSession: P1, hooks: o1, includePartialMessages: m, persistSession: YQ, thinking: t1, effort: t6, maxThinkingTokens: a6, maxTurns: B9, maxBudgetUsd: z9, mcpServers: E0, model: S1, outputFormat: U6, permissionMode: tz = "default", allowDangerouslySkipPermissions: az = false, permissionPromptToolName: sz, plugins: ez, resume: QK, resumeSessionAt: XK, sessionId: YK, stderr: $K, strictMcpConfig: WK } = J, B7 = U6?.type === "json_schema" ? U6.schema : void 0, L6 = K0;
  if (!L6) L6 = { ...process.env };
  if (!L6.CLAUDE_CODE_ENTRYPOINT) L6.CLAUDE_CODE_ENTRYPOINT = "sdk-ts";
  if (U1) L6.CLAUDE_CODE_ENABLE_SDK_FILE_CHECKPOINTING = "true";
  if (!B) throw Error("pathToClaudeCodeExecutable is required");
  let $Q = {}, z7 = /* @__PURE__ */ new Map();
  if (E0) for (let [F6, N6] of Object.entries(E0)) if (N6.type === "sdk" && "instance" in N6) z7.set(F6, N6.instance), $Q[F6] = { type: "sdk", name: F6 };
  else $Q[F6] = N6;
  let JK = typeof Q === "string", s6 = a6;
  if (t1) switch (t1.type) {
    case "adaptive":
      if (!s6) s6 = 32e3;
      break;
    case "enabled":
      s6 = t1.budgetTokens;
      break;
    case "disabled":
      s6 = 0;
      break;
  }
  let K7 = new Q4({ abortController: z, additionalDirectories: K, agent: q, betas: F, cwd: w, debug: A, debugFile: R, executable: V0, executableArgs: s, extraArgs: O0, pathToClaudeCodeExecutable: B, env: L6, forkSession: P1, stderr: $K, maxThinkingTokens: s6, effort: t6, maxTurns: B9, maxBudgetUsd: z9, model: S1, fallbackModel: L0, jsonSchema: B7, permissionMode: tz, allowDangerouslySkipPermissions: az, permissionPromptToolName: sz, continueConversation: N, resume: QK, resumeSessionAt: XK, sessionId: YK, settingSources: $ ?? [], allowedTools: V, disallowedTools: S, tools: C, mcpServers: $Q, strictMcpConfig: WK, canUseTool: !!L, hooks: !!o1, includePartialMessages: m, persistSession: YQ, plugins: ez, sandbox: W, spawnClaudeCodeProcess: J.spawnClaudeCodeProcess }), V7 = new Y4(K7, JK, L, o1, z, z7, B7, { systemPrompt: G, appendSystemPrompt: H, agents: U });
  if (typeof Q === "string") K7.write(Z0({ type: "user", session_id: "", message: { role: "user", content: [{ type: "text", text: Q }] }, parent_tool_use_id: null }) + `
`);
  else V7.streamInput(Q);
  return V7;
}

function send(obj) {
  process.stdout.write(JSON.stringify(obj) + "\n");
}
var rl = createInterface({ input: process.stdin });
function waitForMessage(...expectedTypes) {
  return new Promise((resolve) => {
    const handler = (line) => {
      try {
        const msg = JSON.parse(line);
        if (expectedTypes.includes(msg.type)) {
          rl.off("line", handler);
          resolve(msg);
        }
      } catch {
      }
    };
    rl.on("line", handler);
  });
}
function waitForPermissionResponse(requestId, timeoutMs = 6e4) {
  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      rl.off("line", handler);
      resolve({ behavior: "deny", message: "Permission request timed out" });
    }, timeoutMs);
    const handler = (line) => {
      try {
        const msg = JSON.parse(line);
        if (msg.type === "permission_response" && msg.requestId === requestId) {
          clearTimeout(timer);
          rl.off("line", handler);
          resolve(msg.result);
        }
      } catch {
      }
    };
    rl.on("line", handler);
  });
}
var abortController = new AbortController();
rl.on("line", (line) => {
  try {
    const msg = JSON.parse(line);
    if (msg.type === "abort") {
      abortController.abort();
    }
  } catch {
  }
});
process.on("uncaughtException", (err) => {
  send({ type: "error", message: err.message, fatal: true });
  process.exit(1);
});
function handleStreamEvent(sessionId, event) {
  switch (event.type) {
    case "message_start":
      send({ type: "stream_message_start", sessionId });
      break;
    case "content_block_start": {
      const block = event.content_block;
      const msg = {
        type: "stream_content_start",
        sessionId,
        index: event.index,
        blockType: block.type
      };
      if (block.type === "tool_use") {
        msg.blockId = block.id;
        msg.toolName = block.name;
      }
      send(msg);
      break;
    }
    case "content_block_delta": {
      const delta = event.delta;
      let deltaType;
      let text;
      switch (delta.type) {
        case "text_delta":
          deltaType = "text_delta";
          text = delta.text;
          break;
        case "thinking_delta":
          deltaType = "thinking_delta";
          text = delta.thinking;
          break;
        case "input_json_delta":
          deltaType = "input_json_delta";
          text = delta.partial_json;
          break;
        default:
          return;
      }
      send({
        type: "stream_content_delta",
        sessionId,
        index: event.index,
        deltaType,
        text
      });
      break;
    }
    case "content_block_stop":
      send({ type: "stream_content_stop", sessionId, index: event.index });
      break;
    case "message_delta":
      break;
    case "message_stop":
      send({ type: "stream_message_stop", sessionId });
      break;
  }
}
send({ type: "ready" });
var startMsg = await waitForMessage("start");
var { prompt, options: userOptions = {} } = startMsg;
var requestCounter = 0;
var canUseTool = async (toolName, toolInput, sdkOptions2) => {
  const requestId = `req_${++requestCounter}`;
  send({
    type: "permission_request",
    requestId,
    toolName,
    toolInput,
    toolUseId: sdkOptions2.toolUseID
  });
  return await waitForPermissionResponse(requestId);
};
async function* generateMessages() {
  yield {
    type: "user",
    message: { role: "user", content: prompt },
    parent_tool_use_id: null
  };
  while (true) {
    const msg = await waitForMessage("user_message", "abort");
    if (msg.type === "abort") return;
    const content = msg.images?.length || msg.documents?.length ? [{ type: "text", text: msg.text }, ...msg.images ?? [], ...msg.documents ?? []] : msg.text;
    yield {
      type: "user",
      message: { role: "user", content },
      parent_tool_use_id: null
    };
  }
}
var sdkOptions = {
  cwd: userOptions.cwd,
  resume: userOptions.resume,
  model: userOptions.model,
  systemPrompt: userOptions.systemPrompt,
  settingSources: userOptions.settingSources ?? ["user", "project", "local"],
  disallowedTools: userOptions.disallowedTools,
  maxTurns: userOptions.maxTurns,
  maxThinkingTokens: userOptions.maxThinkingTokens,
  maxBudgetUsd: userOptions.maxBudgetUsd,
  env: userOptions.env,
  abortController,
  canUseTool: userOptions.permissionMode === "default" ? canUseTool : void 0,
  permissionMode: userOptions.permissionMode ?? "default",
  includePartialMessages: userOptions.includePartialMessages !== false,
  pathToClaudeCodeExecutable: userOptions.claudeCodePath || void 0
};
var currentSessionId = null;
try {
  const messageIter = eT({
    prompt: generateMessages(),
    options: sdkOptions
  });
  for await (const message of messageIter) {
    switch (message.type) {
      case "system":
        if (message.subtype === "init") {
          currentSessionId = message.session_id;
          send({
            type: "session_init",
            sessionId: message.session_id,
            model: message.model,
            claudeCodeVersion: message.claude_code_version,
            tools: message.tools,
            mcpServers: message.mcp_servers,
            permissionMode: message.permissionMode
          });
        } else if (message.subtype === "status") {
          send({
            type: "status",
            sessionId: message.session_id,
            status: message.status
          });
        }
        break;
      case "stream_event":
        handleStreamEvent(message.session_id, message.event);
        break;
      case "assistant":
        send({
          type: "assistant_message",
          sessionId: message.session_id,
          parentToolUseId: message.parent_tool_use_id,
          content: message.message.content
        });
        break;
      case "tool_progress":
        send({
          type: "tool_progress",
          sessionId: message.session_id,
          toolName: message.tool_name,
          toolUseId: message.tool_use_id,
          elapsedSeconds: message.elapsed_time_seconds
        });
        break;
      case "result":
        send({
          type: "turn_result",
          sessionId: message.session_id,
          subtype: message.subtype,
          totalCostUsd: message.total_cost_usd,
          numTurns: message.num_turns,
          isError: message.is_error,
          usage: message.usage,
          result: message.subtype === "success" ? message.result : void 0,
          errors: message.subtype !== "success" ? message.errors : void 0
        });
        break;
    }
  }
} catch (err) {
  send({ type: "error", message: err.message, fatal: true });
}
process.exit(0);
