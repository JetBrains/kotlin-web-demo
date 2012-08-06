/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var Kotlin = {};
(function() {
  var h = function() {
  };
  Kotlin.argumentsToArrayLike = function(e) {
    for(var f = e.length, c = Array(f);f--;) {
      c[f] = e[f]
    }
    return c
  };
  (function() {
    function e(c, a) {
      for(var b in a) {
        c[b] = a[b]
      }
      return c
    }
    function f(c) {
      var a = [], b;
      for(b in c) {
        c.hasOwnProperty(b) && a.push(b)
      }
      return a
    }
    e(Object, {extend:e, keys:Object.keys || f, values:function(c) {
      var a = [], b;
      for(b in c) {
        a.push(c[b])
      }
      return a
    }})
  })();
  Object.extend(Function.prototype, function() {
    function e(c, a) {
      for(var b = c.length, d = a.length;d--;) {
        c[b + d] = a[d]
      }
      return c
    }
    var f = Array.prototype.slice;
    return{argumentNames:function() {
      var c = this.toString().match(/^[\s\(]*function[^(]*\(([^)]*)\)/)[1].replace(/\/\/.*?[\r\n]|\/\*(?:.|[\r\n])*?\*\//g, "").replace(/\s+/g, "").split(",");
      return 1 == c.length && !c[0] ? [] : c
    }, bindAsEventListener:function(c) {
      var a = this, b = f.call(arguments, 1);
      return function(d) {
        d = e([d || window.event], b);
        return a.apply(c, d)
      }
    }, wrap:function(c) {
      var a = this;
      return function() {
        var b = e([a.bind(this)], arguments);
        return c.apply(this, b)
      }
    }}
  }());
  Kotlin.isType = function(e, f) {
    if(null === e) {
      return!1
    }
    for(var c = e.get_class();c !== f;) {
      if(null === c) {
        return!1
      }
      c = c.superclass
    }
    return!0
  };
  Kotlin.createTrait = function() {
    return function() {
      for(var e = arguments[0], f = 1, c = arguments.length;f < c;f++) {
        for(var a = e, b = arguments[f], d = Object.keys(b), j = 0, g = d.length;j < g;j++) {
          var i = d[j];
          a[i] = b[i]
        }
      }
      return e
    }
  }();
  Kotlin.definePackage = Kotlin.createTrait;
  Kotlin.createClass = function() {
    function e() {
    }
    var f = {addMethods:function(c) {
      for(var a = this.superclass && this.superclass.prototype, b = Object.keys(c), d = 0, j = b.length;d < j;d++) {
        var g = b[d], i = c[g];
        a && "function" == typeof i && "$super" == i.argumentNames()[0] && (i = function(b) {
          return function() {
            return a[b].apply(this, arguments)
          }
        }(g).wrap(i));
        this.prototype[g] = i
      }
      return this
    }};
    return function() {
      function c() {
        this.initializing = c;
        this.initialize && this.initialize.apply(this, arguments)
      }
      var a = null, b = Kotlin.argumentsToArrayLike(arguments);
      "function" == typeof b[0] && (a = b.shift());
      Object.extend(c, f);
      c.superclass = a;
      c.subclasses = [];
      a && (e.prototype = a.prototype, c.prototype = new e, a.subclasses.push(c));
      c.addMethods({get_class:function() {
        return c
      }});
      null !== a && c.addMethods({super_init:function() {
        this.initializing = this.initializing.superclass;
        this.initializing.prototype.initialize.apply(this, arguments)
      }});
      for(var a = 0, d = b.length;a < d;a++) {
        c.addMethods(b[a])
      }
      c.prototype.initialize || (c.prototype.initialize = h);
      return c.prototype.constructor = c
    }
  }();
  Kotlin.$createClass = Kotlin.createClass;
  Kotlin.$new = function(e) {
    var f = {__proto__:e.prototype};
    return function() {
      e.apply(f, arguments);
      return f
    }
  };
  Kotlin.createObject = function() {
    return new (Kotlin.createClass.apply(null, arguments))
  };
  Kotlin.defineModule = function(e, f) {
    if(e in Kotlin.modules && "JS_TESTS" !== e) {
      throw Kotlin.$new(Kotlin.Exceptions.IllegalArgumentException)();
    }
    Kotlin.modules[e] = f
  }
})();
var kotlin = {set:function(h, e, f) {
  return h.put(e, f)
}};
(function() {
  function h(a) {
    return function() {
      throw new TypeError(void 0 !== a ? "Function " + a + " is abstract" : "Function is abstract");
    }
  }
  Kotlin.equals = function(a, b) {
    if(null === a || void 0 === a) {
      return null === b
    }
    if(a instanceof Array) {
      if(!(b instanceof Array) || a.length != b.length) {
        return!1
      }
      for(var d = 0;d < a.length;d++) {
        if(!Kotlin.equals(a[d], b[d])) {
          return!1
        }
      }
      return!0
    }
    return"object" == typeof a && void 0 !== a.equals ? a.equals(b) : a === b
  };
  Kotlin.array = function(a) {
    var b = [];
    if(null !== a && void 0 !== a) {
      for(var d = 0, c = a.length;d < c;++d) {
        b[d] = a[d]
      }
    }
    return b
  };
  Kotlin.intUpto = function(a, b) {
    return Kotlin.$new(Kotlin.NumberRange)(a, b - a + 1, !1)
  };
  Kotlin.intDownto = function(a, b) {
    return Kotlin.$new(Kotlin.NumberRange)(a, a - b + 1, !0)
  };
  Kotlin.modules = {};
  Kotlin.Exceptions = {};
  Kotlin.Exception = Kotlin.$createClass();
  Kotlin.RuntimeException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.IndexOutOfBounds = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.NullPointerException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.NoSuchElementException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.IllegalArgumentException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.IllegalStateException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.IndexOutOfBoundsException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.UnsupportedOperationException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.Exceptions.IOException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.throwNPE = function() {
    throw Kotlin.$new(Kotlin.Exceptions.NullPointerException)();
  };
  Kotlin.Iterator = Kotlin.$createClass({initialize:function() {
  }, next:h("Iterator#next"), get_hasNext:h("Iterator#get_hasNext")});
  var e = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(a) {
    this.array = a;
    this.size = a.length;
    this.index = 0
  }, next:function() {
    return this.array[this.index++]
  }, get_hasNext:function() {
    return this.index < this.size
  }, hasNext:function() {
    return this.index < this.size
  }}), f = Kotlin.$createClass(e, {initialize:function(a) {
    this.list = a;
    this.size = a.size();
    this.index = 0
  }, next:function() {
    return this.list.get(this.index++)
  }, get_hasNext:function() {
    return this.index < this.size
  }});
  Kotlin.AbstractCollection = Kotlin.$createClass({isEmpty:function() {
    return 0 == this.size()
  }, addAll:function(a) {
    for(a = a.iterator();a.get_hasNext();) {
      this.add(a.next())
    }
  }, equals:function(a) {
    if(null === a || void 0 === a) {
      return!1
    }
    if(this.size() === a.size()) {
      for(var b = this.iterator(), a = a.iterator();;) {
        var d = b.get_hasNext(), c = a.get_hasNext();
        if(d != c) {
          break
        }
        if(c) {
          if(d = b.next(), c = a.next(), !Kotlin.equals(d, c)) {
            break
          }
        }else {
          return!0
        }
      }
    }
    return!1
  }, toString:function() {
    for(var a = "[", b = this.iterator(), d = !0;b.get_hasNext();) {
      d ? d = !1 : a += ", ", a += b.next()
    }
    return a + "]"
  }});
  Kotlin.AbstractList = Kotlin.$createClass(Kotlin.AbstractCollection, {iterator:function() {
    return Kotlin.$new(f)(this)
  }, isEmpty:function() {
    return 0 == this.size()
  }, remove:function(a) {
    a = this.indexOf(a);
    -1 != a && this.removeAt(a)
  }, contains:function(a) {
    return-1 != this.indexOf(a)
  }});
  Kotlin.ArrayList = Kotlin.$createClass(Kotlin.AbstractList, {initialize:function() {
    this.array = [];
    this.$size = 0
  }, get:function(a) {
    if(0 > a || a >= this.$size) {
      throw Kotlin.Exceptions.IndexOutOfBounds;
    }
    return this.array[a]
  }, set:function(a, b) {
    if(0 > a || a >= this.$size) {
      throw Kotlin.Exceptions.IndexOutOfBounds;
    }
    this.array[a] = b
  }, toArray:function() {
    return this.array.slice(0, this.$size)
  }, size:function() {
    return this.$size
  }, iterator:function() {
    return Kotlin.arrayIterator(this.array)
  }, add:function(a) {
    this.array[this.$size++] = a
  }, addAt:function(a, b) {
    this.array.splice(a, 0, b)
  }, removeAt:function(a) {
    this.array.splice(a, 1);
    this.$size--
  }, clear:function() {
    this.$size = this.array.length = 0
  }, indexOf:function(a) {
    for(var b = 0, d = this.$size;b < d;++b) {
      if(Kotlin.equals(this.array[b], a)) {
        return b
      }
    }
    return-1
  }});
  Kotlin.Runnable = Kotlin.$createClass({initialize:function() {
  }, run:h("Runnable#run")});
  Kotlin.Comparable = Kotlin.$createClass({initialize:function() {
  }, compareTo:h("Comparable#compareTo")});
  Kotlin.Appendable = Kotlin.$createClass({initialize:function() {
  }, append:h("Appendable#append")});
  Kotlin.Closeable = Kotlin.$createClass({initialize:function() {
  }, close:h("Closeable#close")});
  Kotlin.parseInt = function(a) {
    return parseInt(a, 10)
  };
  Kotlin.safeParseInt = function(a) {
    a = parseInt(a, 10);
    return isNaN(a) ? null : a
  };
  Kotlin.safeParseDouble = function(a) {
    a = parseFloat(a, 10);
    return isNaN(a) ? null : a
  };
  Kotlin.System = function() {
    var a = "", b = function(b) {
      void 0 !== b && (a = null === b || "object" !== typeof b ? a + b : a + b.toString())
    }, d = function(b) {
      this.print(b);
      a += "\n"
    };
    return{out:function() {
      return{print:b, println:d}
    }, output:function() {
      return a
    }, flush:function() {
      a = ""
    }}
  }();
  Kotlin.println = function(a) {
    Kotlin.System.out().println(a)
  };
  Kotlin.print = function(a) {
    Kotlin.System.out().print(a)
  };
  Kotlin.RangeIterator = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(a, b, d) {
    this.$start = a;
    this.$count = b;
    this.$reversed = d;
    this.$i = this.get_start()
  }, get_start:function() {
    return this.$start
  }, get_count:function() {
    return this.$count
  }, set_count:function(a) {
    this.$count = a
  }, get_reversed:function() {
    return this.$reversed
  }, get_i:function() {
    return this.$i
  }, set_i:function(a) {
    this.$i = a
  }, next:function() {
    this.set_count(this.get_count() - 1);
    if(this.get_reversed()) {
      return this.set_i(this.get_i() - 1), this.get_i() + 1
    }
    this.set_i(this.get_i() + 1);
    return this.get_i() - 1
  }, get_hasNext:function() {
    return 0 < this.get_count()
  }});
  Kotlin.NumberRange = Kotlin.$createClass({initialize:function(a, b, d) {
    this.$start = a;
    this.$size = b;
    this.$reversed = d
  }, get_start:function() {
    return this.$start
  }, get_size:function() {
    return this.$size
  }, get_reversed:function() {
    return this.$reversed
  }, get_end:function() {
    return this.get_reversed() ? this.get_start() - this.get_size() + 1 : this.get_start() + this.get_size() - 1
  }, contains:function(a) {
    return this.get_reversed() ? a <= this.get_start() && a > this.get_start() - this.get_size() : a >= this.get_start() && a < this.get_start() + this.get_size()
  }, iterator:function() {
    return Kotlin.$new(Kotlin.RangeIterator)(this.get_start(), this.get_size(), this.get_reversed())
  }});
  Kotlin.Comparator = Kotlin.$createClass({initialize:function() {
  }, compare:h("Comparator#compare")});
  var c = Kotlin.$createClass(Kotlin.Comparator, {initialize:function(a) {
    this.compare = a
  }});
  Kotlin.comparator = function(a) {
    return Kotlin.$new(c)(a)
  };
  Kotlin.collectionsMax = function(a, b) {
    var d = a.iterator();
    if(a.isEmpty()) {
      throw Kotlin.Exception();
    }
    for(var c = d.next();d.get_hasNext();) {
      var g = d.next();
      0 > b.compare(c, g) && (c = g)
    }
    return c
  };
  Kotlin.StringBuilder = Kotlin.$createClass({initialize:function() {
    this.string = ""
  }, append:function(a) {
    this.string += a.toString()
  }, toString:function() {
    return this.string
  }});
  Kotlin.splitString = function(a, b) {
    return a.split(b)
  };
  Kotlin.nullArray = function(a) {
    for(var b = [];0 < a;) {
      b[--a] = null
    }
    return b
  };
  Kotlin.arrayFromFun = function(a, b) {
    for(var d = [], c = a;0 < c;) {
      d[--c] = b(c)
    }
    return d
  };
  Kotlin.arrayIndices = function(a) {
    return Kotlin.$new(Kotlin.NumberRange)(0, a.length)
  };
  Kotlin.arrayIterator = function(a) {
    return Kotlin.$new(e)(a)
  };
  Kotlin.toString = function(a) {
    return a.toString()
  };
  Kotlin.jsonFromTuples = function(a) {
    for(var b = a.length, c = {};0 < b;) {
      --b, c[a[b][0]] = a[b][1]
    }
    return c
  };
  Kotlin.jsonSet = function(a, b, c) {
    a[b] = c
  };
  Kotlin.jsonGet = function(a, b) {
    return a[b]
  };
  Kotlin.jsonAddProperties = function(a, b) {
    for(var c in b) {
      b.hasOwnProperty(c) && (a[c] = b[c])
    }
    return a
  };
  Kotlin.sure = function(a) {
    return a
  };
  (function() {
    function a(b) {
      return"string" == typeof b ? b : typeof b.hashCode == h ? (b = b.hashCode(), "string" == typeof b ? b : a(b)) : typeof b.toString == h ? b.toString() : "" + b
    }
    function b(a, b) {
      return a.equals(b)
    }
    function c(a, b) {
      return typeof b.equals == h ? b.equals(a) : a === b
    }
    function e(a) {
      return function(b) {
        if(null === b) {
          throw Error("null is not a valid " + a);
        }
        if("undefined" == typeof b) {
          throw Error(a + " must not be undefined");
        }
      }
    }
    function g(a, b, c, d) {
      this[0] = a;
      this.entries = [];
      this.addEntry(b, c);
      null !== d && (this.getEqualityFunction = function() {
        return d
      })
    }
    function i(a) {
      return function(b) {
        for(var c = this.entries.length, d, g = this.getEqualityFunction(b);c--;) {
          if(d = this.entries[c], g(b, d[0])) {
            switch(a) {
              case n:
                return!0;
              case o:
                return d;
              case p:
                return[c, d[1]]
            }
          }
        }
        return!1
      }
    }
    function f(a) {
      return function(b) {
        for(var c = b.length, d = 0, g = this.entries.length;d < g;++d) {
          b[c + d] = this.entries[d][a]
        }
      }
    }
    function k(a, b) {
      var c = a[b];
      return c && c instanceof g ? c : null
    }
    var h = "function", q = typeof Array.prototype.splice == h ? function(a, b) {
      a.splice(b, 1)
    } : function(a, b) {
      var c, d, g;
      if(b === a.length - 1) {
        a.length = b
      }else {
        c = a.slice(b + 1);
        a.length = b;
        d = 0;
        for(g = c.length;d < g;++d) {
          a[b + d] = c[d]
        }
      }
    }, l = e("key"), r = e("value"), n = 0, o = 1, p = 2;
    g.prototype = {getEqualityFunction:function(a) {
      return typeof a.equals == h ? b : c
    }, getEntryForKey:i(o), getEntryAndIndexForKey:i(p), removeEntryForKey:function(a) {
      return(a = this.getEntryAndIndexForKey(a)) ? (q(this.entries, a[0]), a[1]) : null
    }, addEntry:function(a, b) {
      this.entries[this.entries.length] = [a, b]
    }, keys:f(0), values:f(1), getEntries:function(a) {
      for(var b = a.length, c = 0, d = this.entries.length;c < d;++c) {
        a[b + c] = this.entries[c].slice(0)
      }
    }, containsKey:i(n), containsValue:function(a) {
      for(var b = this.entries.length;b--;) {
        if(a === this.entries[b][1]) {
          return!0
        }
      }
      return!1
    }};
    var s = function(b, c) {
      var d = this, e = [], i = {}, f = typeof b == h ? b : a, j = typeof c == h ? c : null;
      this.put = function(a, b) {
        l(a);
        r(b);
        var c = f(a), d, h = null;
        (d = k(i, c)) ? (c = d.getEntryForKey(a)) ? (h = c[1], c[1] = b) : d.addEntry(a, b) : (d = new g(c, a, b, j), e[e.length] = d, i[c] = d);
        return h
      };
      this.get = function(a) {
        l(a);
        var b = f(a);
        if(b = k(i, b)) {
          if(a = b.getEntryForKey(a)) {
            return a[1]
          }
        }
        return null
      };
      this.containsKey = function(a) {
        l(a);
        var b = f(a);
        return(b = k(i, b)) ? b.containsKey(a) : !1
      };
      this.containsValue = function(a) {
        r(a);
        for(var b = e.length;b--;) {
          if(e[b].containsValue(a)) {
            return!0
          }
        }
        return!1
      };
      this.clear = function() {
        e.length = 0;
        i = {}
      };
      this.isEmpty = function() {
        return!e.length
      };
      var m = function(a) {
        return function() {
          for(var b = [], c = e.length;c--;) {
            e[c][a](b)
          }
          return b
        }
      };
      this._keys = m("keys");
      this._values = m("values");
      this._entries = m("getEntries");
      this.values = function() {
        for(var a = this._values(), b = a.length, c = Kotlin.$new(Kotlin.ArrayList)();--b;) {
          c.add(a[b])
        }
        return c
      };
      this.remove = function(a) {
        l(a);
        var b = f(a), c = null, d = k(i, b);
        if(d && (c = d.removeEntryForKey(a), null !== c && !d.entries.length)) {
          a: {
            for(a = e.length;a--;) {
              if(d = e[a], b === d[0]) {
                break a
              }
            }
            a = null
          }
          q(e, a);
          delete i[b]
        }
        return c
      };
      this.size = function() {
        for(var a = 0, b = e.length;b--;) {
          a += e[b].entries.length
        }
        return a
      };
      this.each = function(a) {
        for(var b = d.entries(), c = b.length, g;c--;) {
          g = b[c], a(g[0], g[1])
        }
      };
      this.putAll = function(a, b) {
        for(var c = a.entries(), g, e, i, f = c.length, j = typeof b == h;f--;) {
          g = c[f];
          e = g[0];
          g = g[1];
          if(j && (i = d.get(e))) {
            g = b(e, i, g)
          }
          d.put(e, g)
        }
      };
      this.clone = function() {
        var a = new s(b, c);
        a.putAll(d);
        return a
      };
      this.keySet = function() {
        for(var a = Kotlin.$new(Kotlin.HashSet)(), b = this._keys(), c = b.length;c--;) {
          a.add(b[c])
        }
        return a
      }
    };
    Kotlin.HashTable = s
  })();
  Kotlin.HashMap = Kotlin.$createClass({initialize:function() {
    Kotlin.HashTable.call(this)
  }});
  (function() {
    function a(b, c) {
      var e = new Kotlin.HashTable(b, c);
      this.add = function(a) {
        e.put(a, !0)
      };
      this.addAll = function(a) {
        for(var b = a.length;b--;) {
          e.put(a[b], !0)
        }
      };
      this.values = function() {
        return e._keys()
      };
      this.iterator = function() {
        return Kotlin.arrayIterator(this.values())
      };
      this.remove = function(a) {
        return e.remove(a) ? a : null
      };
      this.contains = function(a) {
        return e.containsKey(a)
      };
      this.clear = function() {
        e.clear()
      };
      this.size = function() {
        return e.size()
      };
      this.isEmpty = function() {
        return e.isEmpty()
      };
      this.clone = function() {
        var g = new a(b, c);
        g.addAll(e.keys());
        return g
      };
      this.equals = function(a) {
        if(null === a || void 0 === a) {
          return!1
        }
        if(this.size() === a.size()) {
          for(var b = this.iterator(), a = a.iterator();;) {
            var c = b.get_hasNext(), d = a.get_hasNext();
            if(c != d) {
              break
            }
            if(d) {
              if(c = b.next(), d = a.next(), !Kotlin.equals(c, d)) {
                break
              }
            }else {
              return!0
            }
          }
        }
        return!1
      };
      this.toString = function() {
        for(var a = "[", b = this.iterator(), c = !0;b.get_hasNext();) {
          c ? c = !1 : a += ", ", a += b.next()
        }
        return a + "]"
      };
      this.intersection = function(g) {
        for(var f = new a(b, c), g = g.values(), h = g.length, k;h--;) {
          k = g[h], e.containsKey(k) && f.add(k)
        }
        return f
      };
      this.union = function(a) {
        for(var b = this.clone(), a = a.values(), c = a.length, d;c--;) {
          d = a[c], e.containsKey(d) || b.add(d)
        }
        return b
      };
      this.isSubsetOf = function(a) {
        for(var b = e.keys(), c = b.length;c--;) {
          if(!a.contains(b[c])) {
            return!1
          }
        }
        return!0
      }
    }
    Kotlin.HashSet = Kotlin.$createClass({initialize:function() {
      a.call(this)
    }})
  })()
})();
