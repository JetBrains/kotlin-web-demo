var Kotlin = {};
(function() {
  function e(b, d) {
    for(var a in d) {
      d.hasOwnProperty(a) && (b[a] = d[a])
    }
  }
  var g = function() {
  };
  Array.isArray || (Array.isArray = function(b) {
    return"[object Array]" === Object.prototype.toString.call(b)
  });
  Function.prototype.bind || (Function.prototype.bind = function(b) {
    if(typeof this !== "function") {
      throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
    }
    var d = Array.prototype.slice.call(arguments, 1), a = this, c = function() {
    }, f = function() {
      return a.apply(this instanceof c && b ? this : b, d.concat(Array.prototype.slice.call(arguments)))
    };
    c.prototype = this.prototype;
    f.prototype = new c;
    return f
  });
  Kotlin.keys = Object.keys || function(b) {
    var d = [], a = 0, c;
    for(c in b) {
      b.hasOwnProperty(c) && (d[a++] = c)
    }
    return d
  };
  Kotlin.isType = function(b, d) {
    if(b === null || b === void 0) {
      return false
    }
    for(var a = b.get_class();a !== d;) {
      if(a === null) {
        return false
      }
      a = a.superclass
    }
    return true
  };
  Kotlin.createTrait = function() {
    for(var b = arguments.length - 1, d = arguments[b] || {}, a = 0;a < b;a++) {
      e(d, arguments[a])
    }
    return d
  };
  Kotlin.definePackage = function(b) {
    return b === null ? {} : b
  };
  Kotlin.createClass = function() {
    function b() {
    }
    function d(a) {
      e(this.prototype, a);
      return this
    }
    function a() {
      if(typeof this.$object$ === "undefined") {
        this.$object$ = this.object_initializer$()
      }
      return this.$object$
    }
    return function(c, f, i) {
      function h() {
        this.initializing = h;
        this.initialize && this.initialize.apply(this, arguments)
      }
      var j = null;
      if(c instanceof Array) {
        j = c;
        c = c[0]
      }
      h.addMethods = d;
      h.superclass = c || null;
      h.subclasses = [];
      h.object$ = a;
      if(c) {
        if(typeof c == "function") {
          b.prototype = c.prototype;
          h.prototype = new b;
          c.subclasses.push(h)
        }else {
          h.addMethods(c)
        }
      }
      h.addMethods({get_class:function() {
        return h
      }});
      c !== null && h.addMethods({super_init:function() {
        this.initializing = this.initializing.superclass;
        this.initializing.prototype.initialize.apply(this, arguments)
      }});
      if(j !== null) {
        for(var c = 1, k = j.length;c < k;c++) {
          h.addMethods(j[c])
        }
      }
      f !== null && f !== void 0 && h.addMethods(f);
      if(!h.prototype.initialize) {
        h.prototype.initialize = g
      }
      h.prototype.constructor = h;
      i !== null && i !== void 0 && e(h, i);
      return h
    }
  }();
  Kotlin.$createClass = function(b, d) {
    if(b !== null && typeof b != "function") {
      d = b;
      b = null
    }
    return Kotlin.createClass(b, d, null)
  };
  Kotlin.createObjectWithPrototype = function(b) {
    function d() {
    }
    d.prototype = b;
    return new d
  };
  Kotlin.$new = function(b) {
    var d = Kotlin.createObjectWithPrototype(b.prototype);
    return function() {
      b.apply(d, arguments);
      return d
    }
  };
  Kotlin.createObject = function() {
    return new (Kotlin.createClass.apply(null, arguments))
  };
  Kotlin.defineModule = function(b, d) {
    if(b in Kotlin.modules) {
      throw Kotlin.$new(Kotlin.IllegalArgumentException)();
    }
    Kotlin.modules[b] = d
  }
})();
String.prototype.startsWith = function(e) {
  return 0 === this.indexOf(e)
};
String.prototype.endsWith = function(e) {
  return-1 !== this.indexOf(e, this.length - e.length)
};
String.prototype.contains = function(e) {
  return-1 !== this.indexOf(e)
};
(function() {
  function e(a) {
    return function() {
      throw new TypeError(void 0 !== a ? "Function " + a + " is abstract" : "Function is abstract");
    }
  }
  Kotlin.equals = function(a, c) {
    return null == a ? null == c : Array.isArray(a) ? Kotlin.arrayEquals(a, c) : "object" == typeof a && void 0 !== a.equals ? a.equals(c) : a === c
  };
  Kotlin.toString = function(a) {
    return null == a ? "null" : Array.isArray(a) ? Kotlin.arrayToString(a) : a.toString()
  };
  Kotlin.arrayToString = function(a) {
    return"[" + a.join(", ") + "]"
  };
  Kotlin.intUpto = function(a, c) {
    return Kotlin.$new(Kotlin.NumberRange)(a, c)
  };
  Kotlin.intDownto = function(a, c) {
    return Kotlin.$new(Kotlin.Progression)(a, c, -1)
  };
  Kotlin.modules = {};
  Kotlin.RuntimeException = Kotlin.$createClass();
  Kotlin.NullPointerException = Kotlin.$createClass();
  Kotlin.NoSuchElementException = Kotlin.$createClass();
  Kotlin.IllegalArgumentException = Kotlin.$createClass();
  Kotlin.IllegalStateException = Kotlin.$createClass();
  Kotlin.UnsupportedOperationException = Kotlin.$createClass();
  Kotlin.IOException = Kotlin.$createClass();
  Kotlin.throwNPE = function() {
    throw Kotlin.$new(Kotlin.NullPointerException)();
  };
  Kotlin.Iterator = Kotlin.$createClass({initialize:function() {
  }, next:e("Iterator#next"), hasNext:e("Iterator#hasNext")});
  var g = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(a) {
    this.array = a;
    this.size = a.length;
    this.index = 0
  }, next:function() {
    return this.array[this.index++]
  }, hasNext:function() {
    return this.index < this.size
  }}), b = Kotlin.$createClass(g, {initialize:function(a) {
    this.list = a;
    this.size = a.size();
    this.index = 0
  }, next:function() {
    return this.list.get(this.index++)
  }});
  Kotlin.Collection = Kotlin.$createClass();
  Kotlin.Enum = Kotlin.$createClass(null, {initialize:function() {
    this.ordinal$ = this.name$ = void 0
  }, name:function() {
    return this.name$
  }, ordinal:function() {
    return this.ordinal$
  }, toString:function() {
    return this.name()
  }});
  (function() {
    function a(a) {
      return this[a]
    }
    function c() {
      return this.values$
    }
    Kotlin.createEnumEntries = function(f) {
      var b = 0, d = [], e;
      for(e in f) {
        if(f.hasOwnProperty(e)) {
          var g = f[e];
          d[b] = g;
          g.ordinal$ = b;
          g.name$ = e;
          b++
        }
      }
      f.values$ = d;
      f.valueOf = a;
      f.values = c;
      return f
    }
  })();
  Kotlin.AbstractCollection = Kotlin.$createClass(Kotlin.Collection, {size:function() {
    return this.$size
  }, addAll:function(a) {
    for(var a = a.iterator(), c = this.size();0 < c--;) {
      this.add(a.next())
    }
  }, isEmpty:function() {
    return 0 === this.size()
  }, iterator:function() {
    return Kotlin.$new(g)(this.toArray())
  }, equals:function(a) {
    if(this.size() !== a.size()) {
      return!1
    }
    for(var c = this.iterator(), a = a.iterator(), f = this.size();0 < f--;) {
      if(!Kotlin.equals(c.next(), a.next())) {
        return!1
      }
    }
    return!0
  }, toString:function() {
    for(var a = "[", c = this.iterator(), f = !0, b = this.$size;0 < b--;) {
      f ? f = !1 : a += ", ", a += c.next()
    }
    return a + "]"
  }, toJSON:function() {
    return this.toArray()
  }});
  Kotlin.AbstractList = Kotlin.$createClass(Kotlin.AbstractCollection, {iterator:function() {
    return Kotlin.$new(b)(this)
  }, remove:function(a) {
    a = this.indexOf(a);
    -1 !== a && this.removeAt(a)
  }, contains:function(a) {
    return-1 !== this.indexOf(a)
  }});
  Kotlin.ArrayList = Kotlin.$createClass(Kotlin.AbstractList, {initialize:function() {
    this.array = [];
    this.$size = 0
  }, get:function(a) {
    this.checkRange(a);
    return this.array[a]
  }, set:function(a, c) {
    this.checkRange(a);
    this.array[a] = c
  }, size:function() {
    return this.$size
  }, iterator:function() {
    return Kotlin.arrayIterator(this.array)
  }, add:function(a) {
    this.array[this.$size++] = a
  }, addAt:function(a, c) {
    this.array.splice(a, 0, c);
    this.$size++
  }, addAll:function(a) {
    for(var c = a.iterator(), f = this.$size, b = a.size();0 < b--;) {
      this.array[f++] = c.next()
    }
    this.$size += a.size()
  }, removeAt:function(a) {
    this.checkRange(a);
    this.$size--;
    return this.array.splice(a, 1)[0]
  }, clear:function() {
    this.$size = this.array.length = 0
  }, indexOf:function(a) {
    for(var c = 0, b = this.$size;c < b;++c) {
      if(Kotlin.equals(this.array[c], a)) {
        return c
      }
    }
    return-1
  }, toArray:function() {
    return this.array.slice(0, this.$size)
  }, toString:function() {
    return"[" + this.array.join(", ") + "]"
  }, toJSON:function() {
    return this.array
  }, checkRange:function(a) {
    if(0 > a || a >= this.$size) {
      throw new RangeError;
    }
  }});
  Kotlin.Runnable = Kotlin.$createClass({initialize:function() {
  }, run:e("Runnable#run")});
  Kotlin.Comparable = Kotlin.$createClass({initialize:function() {
  }, compareTo:e("Comparable#compareTo")});
  Kotlin.Appendable = Kotlin.$createClass({initialize:function() {
  }, append:e("Appendable#append")});
  Kotlin.Closeable = Kotlin.$createClass({initialize:function() {
  }, close:e("Closeable#close")});
  Kotlin.safeParseInt = function(a) {
    a = parseInt(a, 10);
    return isNaN(a) ? null : a
  };
  Kotlin.safeParseDouble = function(a) {
    a = parseFloat(a);
    return isNaN(a) ? null : a
  };
  Kotlin.arrayEquals = function(a, c) {
    if(a === c) {
      return!0
    }
    if(!Array.isArray(c) || a.length !== c.length) {
      return!1
    }
    for(var b = 0, d = a.length;b < d;b++) {
      if(!Kotlin.equals(a[b], c[b])) {
        return!1
      }
    }
    return!0
  };
  Kotlin.System = function() {
    var a = "", c = function(c) {
      void 0 !== c && (a = null === c || "object" !== typeof c ? a + c : a + c.toString())
    }, b = function(c) {
      this.print(c);
      a += "\n"
    };
    return{out:function() {
      return{print:c, println:b}
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
  Kotlin.RangeIterator = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(a, c, b) {
    this.$start = a;
    this.$end = c;
    this.$increment = b;
    this.$i = a
  }, get_start:function() {
    return this.$start
  }, get_end:function() {
    return this.$end
  }, get_i:function() {
    return this.$i
  }, set_i:function(a) {
    this.$i = a
  }, next:function() {
    var a = this.$i;
    this.set_i(this.$i + this.$increment);
    return a
  }, hasNext:function() {
    return 0 < this.get_count()
  }});
  Kotlin.NumberRange = Kotlin.$createClass({initialize:function(a, c) {
    this.$start = a;
    this.$end = c
  }, get_start:function() {
    return this.$start
  }, get_end:function() {
    return this.$end
  }, get_increment:function() {
    return 1
  }, contains:function(a) {
    return this.$start <= a && a <= this.$end
  }, iterator:function() {
    return Kotlin.$new(Kotlin.RangeIterator)(this.get_start(), this.get_end())
  }});
  Kotlin.Progression = Kotlin.$createClass({initialize:function(a, c, b) {
    this.$start = a;
    this.$end = c;
    this.$increment = b
  }, get_start:function() {
    return this.$start
  }, get_end:function() {
    return this.$end
  }, get_increment:function() {
    return this.$increment
  }, iterator:function() {
    return Kotlin.$new(Kotlin.RangeIterator)(this.get_start(), this.get_end(), this.get_increment())
  }});
  Kotlin.Comparator = Kotlin.$createClass({initialize:function() {
  }, compare:e("Comparator#compare")});
  var d = Kotlin.$createClass(Kotlin.Comparator, {initialize:function(a) {
    this.compare = a
  }});
  Kotlin.comparator = function(a) {
    return Kotlin.$new(d)(a)
  };
  Kotlin.collectionsMax = function(a, c) {
    if(a.isEmpty()) {
      throw Error();
    }
    for(var b = a.iterator(), d = b.next();b.hasNext();) {
      var e = b.next();
      0 > c.compare(d, e) && (d = e)
    }
    return d
  };
  Kotlin.collectionsSort = function(a, c) {
    var b = void 0;
    void 0 !== c && (b = c.compare.bind(c));
    a instanceof Array && a.sort(b);
    for(var d = [], e = a.iterator();e.hasNext();) {
      d.push(e.next())
    }
    d.sort(b);
    b = 0;
    for(e = d.length;b < e;b++) {
      a.set(b, d[b])
    }
  };
  Kotlin.StringBuilder = Kotlin.$createClass({initialize:function() {
    this.string = ""
  }, append:function(a) {
    this.string += a.toString()
  }, toString:function() {
    return this.string
  }});
  Kotlin.splitString = function(a, c, b) {
    return a.split(RegExp(c), b)
  };
  Kotlin.nullArray = function(a) {
    for(var c = [];0 < a;) {
      c[--a] = null
    }
    return c
  };
  Kotlin.numberArrayOfSize = function(a) {
    return Kotlin.arrayFromFun(a, function() {
      return 0
    })
  };
  Kotlin.charArrayOfSize = function(a) {
    return Kotlin.arrayFromFun(a, function() {
      return"\x00"
    })
  };
  Kotlin.booleanArrayOfSize = function(a) {
    return Kotlin.arrayFromFun(a, function() {
      return!1
    })
  };
  Kotlin.arrayFromFun = function(a, c) {
    for(var b = Array(a), d = 0;d < a;d++) {
      b[d] = c(d)
    }
    return b
  };
  Kotlin.arrayIndices = function(a) {
    return Kotlin.$new(Kotlin.NumberRange)(0, a.length - 1)
  };
  Kotlin.arrayIterator = function(a) {
    return Kotlin.$new(g)(a)
  };
  Kotlin.jsonFromTuples = function(a) {
    for(var c = a.length, b = {};0 < c;) {
      --c, b[a[c][0]] = a[c][1]
    }
    return b
  };
  Kotlin.jsonAddProperties = function(a, c) {
    for(var b in c) {
      c.hasOwnProperty(b) && (a[b] = c[b])
    }
    return a
  }
})();
Kotlin.assignOwner = function(e, g) {
  e.o = g;
  return e
};
(function() {
  function e(a) {
    return"string" == typeof a ? a : typeof a.hashCode == h ? (a = a.hashCode(), "string" == typeof a ? a : e(a)) : typeof a.toString == h ? a.toString() : "" + a
  }
  function g(a, b) {
    return a.equals(b)
  }
  function b(a, b) {
    return typeof b.equals == h ? b.equals(a) : a === b
  }
  function d(a) {
    return function(b) {
      if(null === b) {
        throw Error("null is not a valid " + a);
      }
      if("undefined" == typeof b) {
        throw Error(a + " must not be undefined");
      }
    }
  }
  function a(a, b, c, d) {
    this[0] = a;
    this.entries = [];
    this.addEntry(b, c);
    null !== d && (this.getEqualityFunction = function() {
      return d
    })
  }
  function c(a) {
    return function(b) {
      for(var c = this.entries.length, d, e = this.getEqualityFunction(b);c--;) {
        if(d = this.entries[c], e(b, d[0])) {
          switch(a) {
            case l:
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
      for(var c = b.length, d = 0, e = this.entries.length;d < e;++d) {
        b[c + d] = this.entries[d][a]
      }
    }
  }
  function i(b, c) {
    var d = b[c];
    return d && d instanceof a ? d : null
  }
  var h = "function", j = typeof Array.prototype.splice == h ? function(a, b) {
    a.splice(b, 1)
  } : function(a, b) {
    var c, d, e;
    if(b === a.length - 1) {
      a.length = b
    }else {
      c = a.slice(b + 1);
      a.length = b;
      d = 0;
      for(e = c.length;d < e;++d) {
        a[b + d] = c[d]
      }
    }
  }, k = d("key"), q = d("value"), l = 0, o = 1, p = 2;
  a.prototype = {getEqualityFunction:function(a) {
    return typeof a.equals == h ? g : b
  }, getEntryForKey:c(o), getEntryAndIndexForKey:c(p), removeEntryForKey:function(a) {
    return(a = this.getEntryAndIndexForKey(a)) ? (j(this.entries, a[0]), a[1]) : null
  }, addEntry:function(a, b) {
    this.entries[this.entries.length] = [a, b]
  }, keys:f(0), values:f(1), getEntries:function(a) {
    for(var b = a.length, c = 0, d = this.entries.length;c < d;++c) {
      a[b + c] = this.entries[c].slice(0)
    }
  }, containsKey:c(l), containsValue:function(a) {
    for(var b = this.entries.length;b--;) {
      if(a === this.entries[b][1]) {
        return!0
      }
    }
    return!1
  }};
  var r = function(b, c) {
    var d = this, f = [], g = {}, m = typeof b == h ? b : e, l = typeof c == h ? c : null;
    this.put = function(b, c) {
      k(b);
      q(c);
      var d = m(b), e, h = null;
      (e = i(g, d)) ? (d = e.getEntryForKey(b)) ? (h = d[1], d[1] = c) : e.addEntry(b, c) : (e = new a(d, b, c, l), f[f.length] = e, g[d] = e);
      return h
    };
    this.get = function(a) {
      k(a);
      var b = m(a);
      if(b = i(g, b)) {
        if(a = b.getEntryForKey(a)) {
          return a[1]
        }
      }
      return null
    };
    this.containsKey = function(a) {
      k(a);
      var b = m(a);
      return(b = i(g, b)) ? b.containsKey(a) : !1
    };
    this.containsValue = function(a) {
      q(a);
      for(var b = f.length;b--;) {
        if(f[b].containsValue(a)) {
          return!0
        }
      }
      return!1
    };
    this.clear = function() {
      f.length = 0;
      g = {}
    };
    this.isEmpty = function() {
      return!f.length
    };
    var n = function(a) {
      return function() {
        for(var b = [], c = f.length;c--;) {
          f[c][a](b)
        }
        return b
      }
    };
    this._keys = n("keys");
    this._values = n("values");
    this._entries = n("getEntries");
    this.values = function() {
      for(var a = this._values(), b = a.length, c = Kotlin.$new(Kotlin.ArrayList)();b--;) {
        c.add(a[b])
      }
      return c
    };
    this.remove = function(a) {
      k(a);
      var b = m(a), c = null, d = i(g, b);
      if(d && (c = d.removeEntryForKey(a), null !== c && !d.entries.length)) {
        a: {
          for(a = f.length;a--;) {
            if(d = f[a], b === d[0]) {
              break a
            }
          }
          a = null
        }
        j(f, a);
        delete g[b]
      }
      return c
    };
    this.size = function() {
      for(var a = 0, b = f.length;b--;) {
        a += f[b].entries.length
      }
      return a
    };
    this.each = function(a) {
      for(var b = d._entries(), c = b.length, e;c--;) {
        e = b[c], a(e[0], e[1])
      }
    };
    this.putAll = function(a, b) {
      for(var c = a._entries(), e, f, g, i = c.length, j = typeof b == h;i--;) {
        e = c[i];
        f = e[0];
        e = e[1];
        if(j && (g = d.get(f))) {
          e = b(f, g, e)
        }
        d.put(f, e)
      }
    };
    this.clone = function() {
      var a = new r(b, c);
      a.putAll(d);
      return a
    };
    this.keySet = function() {
      for(var a = Kotlin.$new(Kotlin.ComplexHashSet)(), b = this._keys(), c = b.length;c--;) {
        a.add(b[c])
      }
      return a
    }
  };
  Kotlin.HashTable = r
})();
Kotlin.Map = Kotlin.$createClass();
Kotlin.HashMap = Kotlin.$createClass(Kotlin.Map, {initialize:function() {
  Kotlin.HashTable.call(this)
}});
Kotlin.ComplexHashMap = Kotlin.HashMap;
(function() {
  var e = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(b, d) {
    this.map = b;
    this.keys = d;
    this.size = d.length;
    this.index = 0
  }, next:function() {
    return this.map[this.keys[this.index++]]
  }, hasNext:function() {
    return this.index < this.size
  }}), g = Kotlin.$createClass(Kotlin.Collection, {initialize:function(b) {
    this.map = b
  }, iterator:function() {
    return Kotlin.$new(e)(this.map.map, Kotlin.keys(this.map.map))
  }, isEmpty:function() {
    return 0 === this.map.$size
  }, contains:function(b) {
    return this.map.containsValue(b)
  }});
  Kotlin.PrimitiveHashMap = Kotlin.$createClass(Kotlin.Map, {initialize:function() {
    this.$size = 0;
    this.map = {}
  }, size:function() {
    return this.$size
  }, isEmpty:function() {
    return 0 === this.$size
  }, containsKey:function(b) {
    return void 0 !== this.map[b]
  }, containsValue:function(b) {
    var d = this.map, a;
    for(a in d) {
      if(d.hasOwnProperty(a) && d[a] === b) {
        return!0
      }
    }
    return!1
  }, get:function(b) {
    return this.map[b]
  }, put:function(b, d) {
    var a = this.map[b];
    this.map[b] = void 0 === d ? null : d;
    void 0 === a && this.$size++;
    return a
  }, remove:function(b) {
    var d = this.map[b];
    void 0 !== d && (delete this.map[b], this.$size--);
    return d
  }, clear:function() {
    this.$size = 0;
    this.map = {}
  }, putAll:function(b) {
    var b = b.map, d;
    for(d in b) {
      b.hasOwnProperty(d) && (this.map[d] = b[d], this.$size++)
    }
  }, keySet:function() {
    var b = Kotlin.$new(Kotlin.PrimitiveHashSet)(), d = this.map, a;
    for(a in d) {
      d.hasOwnProperty(a) && b.add(a)
    }
    return b
  }, values:function() {
    return Kotlin.$new(g)(this)
  }, toJSON:function() {
    return this.map
  }})
})();
Kotlin.Set = Kotlin.$createClass(Kotlin.Collection);
Kotlin.PrimitiveHashSet = Kotlin.$createClass(Kotlin.AbstractCollection, {initialize:function() {
  this.$size = 0;
  this.map = {}
}, contains:function(e) {
  return!0 === this.map[e]
}, add:function(e) {
  var g = this.map[e];
  this.map[e] = !0;
  if(!0 === g) {
    return!1
  }
  this.$size++;
  return!0
}, remove:function(e) {
  return!0 === this.map[e] ? (delete this.map[e], this.$size--, !0) : !1
}, clear:function() {
  this.$size = 0;
  this.map = {}
}, toArray:function() {
  return Kotlin.keys(this.map)
}});
(function() {
  function e(g, b) {
    var d = new Kotlin.HashTable(g, b);
    this.add = function(a) {
      d.put(a, !0)
    };
    this.addAll = function(a) {
      for(var b = a.length;b--;) {
        d.put(a[b], !0)
      }
    };
    this.values = function() {
      return d._keys()
    };
    this.iterator = function() {
      return Kotlin.arrayIterator(this.values())
    };
    this.remove = function(a) {
      return d.remove(a) ? a : null
    };
    this.contains = function(a) {
      return d.containsKey(a)
    };
    this.clear = function() {
      d.clear()
    };
    this.size = function() {
      return d.size()
    };
    this.isEmpty = function() {
      return d.isEmpty()
    };
    this.clone = function() {
      var a = new e(g, b);
      a.addAll(d.keys());
      return a
    };
    this.equals = function(a) {
      if(null === a || void 0 === a) {
        return!1
      }
      if(this.size() === a.size()) {
        for(var b = this.iterator(), a = a.iterator();;) {
          var d = b.hasNext(), e = a.hasNext();
          if(d != e) {
            break
          }
          if(e) {
            if(d = b.next(), e = a.next(), !Kotlin.equals(d, e)) {
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
      for(var a = "[", b = this.iterator(), d = !0;b.hasNext();) {
        d ? d = !1 : a += ", ", a += b.next()
      }
      return a + "]"
    };
    this.intersection = function(a) {
      for(var c = new e(g, b), a = a.values(), f = a.length, i;f--;) {
        i = a[f], d.containsKey(i) && c.add(i)
      }
      return c
    };
    this.union = function(a) {
      for(var b = this.clone(), a = a.values(), e = a.length, g;e--;) {
        g = a[e], d.containsKey(g) || b.add(g)
      }
      return b
    };
    this.isSubsetOf = function(a) {
      for(var b = d.keys(), e = b.length;e--;) {
        if(!a.contains(b[e])) {
          return!1
        }
      }
      return!0
    }
  }
  Kotlin.HashSet = Kotlin.$createClass(Kotlin.Set, {initialize:function() {
    e.call(this)
  }});
  Kotlin.ComplexHashSet = Kotlin.HashSet
})();

