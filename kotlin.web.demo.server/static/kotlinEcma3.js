var Kotlin = {};
(function() {
  function e(b, c) {
    for(var a in c) {
      c.hasOwnProperty(a) && (b[a] = c[a])
    }
  }
  var h = function() {
  };
  Function.prototype.bind || (Function.prototype.bind = function(b) {
    if("function" !== typeof this) {
      throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
    }
    var c = Array.prototype.slice.call(arguments, 1), a = this, d = function() {
    }, g = function() {
      return a.apply(this instanceof d && b ? this : b, c.concat(Array.prototype.slice.call(arguments)))
    };
    d.prototype = this.prototype;
    g.prototype = new d;
    return g
  });
  Kotlin.keys = Object.keys || function(b) {
    var c = [], a = 0, d;
    for(d in b) {
      b.hasOwnProperty(d) && (c[a++] = d)
    }
    return c
  };
  Kotlin.isType = function(b, c) {
    if(b === null || b === void 0) {
      return false
    }
    for(var a = b.get_class();a !== c;) {
      if(a === null) {
        return false
      }
      a = a.superclass
    }
    return true
  };
  Kotlin.createTrait = function() {
    for(var b = arguments.length - 1, c = arguments[b] || {}, a = 0;a < b;a++) {
      e(c, arguments[a])
    }
    return c
  };
  Kotlin.definePackage = function(b) {
    return b === null ? {} : b
  };
  Kotlin.createClass = function() {
    function b() {
    }
    function c(a) {
      e(this.prototype, a);
      return this
    }
    return function(a, d, g) {
      function f() {
        this.initializing = f;
        this.initialize && this.initialize.apply(this, arguments)
      }
      var i = null;
      if(a instanceof Array) {
        i = a;
        a = a[0]
      }
      f.addMethods = c;
      f.superclass = a || null;
      f.subclasses = [];
      if(a) {
        if(typeof a == "function") {
          b.prototype = a.prototype;
          f.prototype = new b;
          a.subclasses.push(f)
        }else {
          f.addMethods(a)
        }
      }
      f.addMethods({get_class:function() {
        return f
      }});
      a !== null && f.addMethods({super_init:function() {
        this.initializing = this.initializing.superclass;
        this.initializing.prototype.initialize.apply(this, arguments)
      }});
      if(i !== null) {
        for(var a = 1, j = i.length;a < j;a++) {
          f.addMethods(i[a])
        }
      }
      d !== null && d !== void 0 && f.addMethods(d);
      if(!f.prototype.initialize) {
        f.prototype.initialize = h
      }
      f.prototype.constructor = f;
      g !== null && g !== void 0 && e(f, g);
      return f
    }
  }();
  Kotlin.$createClass = function(b, c) {
    if(b !== null && typeof b != "function") {
      c = b;
      b = null
    }
    return Kotlin.createClass(b, c, null)
  };
  Kotlin.createObjectWithPrototype = function(b) {
    function c() {
    }
    c.prototype = b;
    return new c
  };
  Kotlin.$new = function(b) {
    var c = Kotlin.createObjectWithPrototype(b.prototype);
    return function() {
      b.apply(c, arguments);
      return c
    }
  };
  Kotlin.createObject = function() {
    return new (Kotlin.createClass.apply(null, arguments))
  };
  Kotlin.defineModule = function(b, c) {
    if(b in Kotlin.modules) {
      throw Kotlin.$new(Kotlin.IllegalArgumentException)();
    }
    Kotlin.modules[b] = c
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
var kotlin = {set:function(e, h, b) {
  return e.put(h, b)
}};
(function() {
  function e(a) {
    return function() {
      throw new TypeError(void 0 !== a ? "Function " + a + " is abstract" : "Function is abstract");
    }
  }
  Kotlin.equals = function(a, d) {
    if(null == a) {
      return null == d
    }
    if(a instanceof Array) {
      if(!(d instanceof Array) || a.length != d.length) {
        return!1
      }
      for(var b = 0;b < a.length;b++) {
        if(!Kotlin.equals(a[b], d[b])) {
          return!1
        }
      }
      return!0
    }
    return"object" == typeof a && void 0 !== a.equals ? a.equals(d) : a === d
  };
  Kotlin.array = function(a) {
    return null === a || void 0 === a ? [] : a.slice()
  };
  Kotlin.intUpto = function(a, d) {
    return Kotlin.$new(Kotlin.NumberRange)(a, d)
  };
  Kotlin.intDownto = function(a, d) {
    return Kotlin.$new(Kotlin.Progression)(a, d, -1)
  };
  Kotlin.modules = {};
  Kotlin.Exception = Kotlin.$createClass();
  Kotlin.RuntimeException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.IndexOutOfBounds = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.NullPointerException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.NoSuchElementException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.IllegalArgumentException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.IllegalStateException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.IndexOutOfBoundsException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.UnsupportedOperationException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.IOException = Kotlin.$createClass(Kotlin.Exception);
  Kotlin.throwNPE = function() {
    throw Kotlin.$new(Kotlin.NullPointerException)();
  };
  Kotlin.Iterator = Kotlin.$createClass({initialize:function() {
  }, next:e("Iterator#next"), hasNext:e("Iterator#hasNext")});
  var h = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(a) {
    this.array = a;
    this.size = a.length;
    this.index = 0
  }, next:function() {
    return this.array[this.index++]
  }, hasNext:function() {
    return this.index < this.size
  }}), b = Kotlin.$createClass(h, {initialize:function(a) {
    this.list = a;
    this.size = a.size();
    this.index = 0
  }, next:function() {
    return this.list.get(this.index++)
  }});
  Kotlin.Collection = Kotlin.$createClass();
  Kotlin.AbstractCollection = Kotlin.$createClass(Kotlin.Collection, {size:function() {
    return this.$size
  }, addAll:function(a) {
    for(var a = a.iterator(), d = this.size();0 < d--;) {
      this.add(a.next())
    }
  }, isEmpty:function() {
    return 0 === this.size()
  }, iterator:function() {
    return Kotlin.$new(h)(this.toArray())
  }, equals:function(a) {
    if(this.size() === a.size()) {
      for(var d = this.iterator(), a = a.iterator(), b = this.size();0 < b--;) {
        if(!Kotlin.equals(d.next(), a.next())) {
          return!1
        }
      }
    }
    return!0
  }, toString:function() {
    for(var a = "[", d = this.iterator(), b = !0, c = this.$size;0 < c--;) {
      b ? b = !1 : a += ", ", a += d.next()
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
  }, set:function(a, d) {
    this.checkRange(a);
    this.array[a] = d
  }, size:function() {
    return this.$size
  }, iterator:function() {
    return Kotlin.arrayIterator(this.array)
  }, add:function(a) {
    this.array[this.$size++] = a
  }, addAt:function(a, d) {
    this.array.splice(a, 0, d);
    this.$size++
  }, addAll:function(a) {
    for(var d = a.iterator(), b = this.$size, c = a.size();0 < c--;) {
      this.array[b++] = d.next()
    }
    this.$size += a.size()
  }, removeAt:function(a) {
    this.checkRange(a);
    this.$size--;
    return this.array.splice(a, 1)[0]
  }, clear:function() {
    this.$size = this.array.length = 0
  }, indexOf:function(a) {
    for(var d = 0, b = this.$size;d < b;++d) {
      if(Kotlin.equals(this.array[d], a)) {
        return d
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
      throw new Kotlin.IndexOutOfBoundsException;
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
  Kotlin.parseInt = function(a) {
    return parseInt(a, 10)
  };
  Kotlin.safeParseInt = function(a) {
    a = parseInt(a, 10);
    return isNaN(a) ? null : a
  };
  Kotlin.safeParseDouble = function(a) {
    a = parseFloat(a);
    return isNaN(a) ? null : a
  };
  Kotlin.System = function() {
    var a = "", d = function(d) {
      void 0 !== d && (a = null === d || "object" !== typeof d ? a + d : a + d.toString())
    }, b = function(d) {
      this.print(d);
      a += "\n"
    };
    return{out:function() {
      return{print:d, println:b}
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
  Kotlin.RangeIterator = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(a, d, b) {
    this.$start = a;
    this.$end = d;
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
  Kotlin.NumberRange = Kotlin.$createClass({initialize:function(a, d) {
    this.$start = a;
    this.$end = d
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
  Kotlin.Progression = Kotlin.$createClass({initialize:function(a, d, b) {
    this.$start = a;
    this.$end = d;
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
  var c = Kotlin.$createClass(Kotlin.Comparator, {initialize:function(a) {
    this.compare = a
  }});
  Kotlin.comparator = function(a) {
    return Kotlin.$new(c)(a)
  };
  Kotlin.collectionsMax = function(a, d) {
    var b = a.iterator();
    if(a.isEmpty()) {
      throw Kotlin.Exception();
    }
    for(var c = b.next();b.hasNext();) {
      var e = b.next();
      0 > d.compare(c, e) && (c = e)
    }
    return c
  };
  Kotlin.collectionsSort = function(a, d) {
    var b = void 0;
    void 0 !== d && (b = d.compare.bind(d));
    a instanceof Array && a.sort(b);
    for(var c = [], e = a.iterator();e.hasNext();) {
      c.push(e.next())
    }
    c.sort(b);
    b = 0;
    for(e = c.length;b < e;b++) {
      a.set(b, c[b])
    }
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
  Kotlin.arrayFromFun = function(a, b) {
    for(var c = Array(a), e = 0;e < a;e++) {
      c[e] = b(e)
    }
    return c
  };
  Kotlin.arrayIndices = function(a) {
    return Kotlin.$new(Kotlin.NumberRange)(0, a.length - 1)
  };
  Kotlin.arrayIterator = function(a) {
    return Kotlin.$new(h)(a)
  };
  Kotlin.toString = function(a) {
    return a.toString()
  };
  Kotlin.jsonFromPairs = function(a) {
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
  }
})();
Kotlin.assignOwner = function(e, h) {
  e.o = h;
  return e
};
(function() {
  function e(a) {
    return"string" == typeof a ? a : typeof a.hashCode == i ? (a = a.hashCode(), "string" == typeof a ? a : e(a)) : typeof a.toString == i ? a.toString() : "" + a
  }
  function h(a, b) {
    return a.equals(b)
  }
  function b(a, b) {
    return typeof b.equals == i ? b.equals(a) : a === b
  }
  function c(a) {
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
  function d(a) {
    return function(b) {
      for(var c = this.entries.length, d, e = this.getEqualityFunction(b);c--;) {
        if(d = this.entries[c], e(b, d[0])) {
          switch(a) {
            case k:
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
  function g(a) {
    return function(b) {
      for(var c = b.length, d = 0, e = this.entries.length;d < e;++d) {
        b[c + d] = this.entries[d][a]
      }
    }
  }
  function f(b, c) {
    var d = b[c];
    return d && d instanceof a ? d : null
  }
  var i = "function", j = typeof Array.prototype.splice == i ? function(a, b) {
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
  }, l = c("key"), q = c("value"), k = 0, o = 1, p = 2;
  a.prototype = {getEqualityFunction:function(a) {
    return typeof a.equals == i ? h : b
  }, getEntryForKey:d(o), getEntryAndIndexForKey:d(p), removeEntryForKey:function(a) {
    return(a = this.getEntryAndIndexForKey(a)) ? (j(this.entries, a[0]), a[1]) : null
  }, addEntry:function(a, b) {
    this.entries[this.entries.length] = [a, b]
  }, keys:g(0), values:g(1), getEntries:function(a) {
    for(var b = a.length, d = 0, c = this.entries.length;d < c;++d) {
      a[b + d] = this.entries[d].slice(0)
    }
  }, containsKey:d(k), containsValue:function(a) {
    for(var b = this.entries.length;b--;) {
      if(a === this.entries[b][1]) {
        return!0
      }
    }
    return!1
  }};
  var r = function(b, d) {
    var c = this, g = [], h = {}, m = typeof b == i ? b : e, k = typeof d == i ? d : null;
    this.put = function(b, d) {
      l(b);
      q(d);
      var c = m(b), e, i = null;
      (e = f(h, c)) ? (c = e.getEntryForKey(b)) ? (i = c[1], c[1] = d) : e.addEntry(b, d) : (e = new a(c, b, d, k), g[g.length] = e, h[c] = e);
      return i
    };
    this.get = function(a) {
      l(a);
      var b = m(a);
      if(b = f(h, b)) {
        if(a = b.getEntryForKey(a)) {
          return a[1]
        }
      }
      return null
    };
    this.containsKey = function(a) {
      l(a);
      var b = m(a);
      return(b = f(h, b)) ? b.containsKey(a) : !1
    };
    this.containsValue = function(a) {
      q(a);
      for(var b = g.length;b--;) {
        if(g[b].containsValue(a)) {
          return!0
        }
      }
      return!1
    };
    this.clear = function() {
      g.length = 0;
      h = {}
    };
    this.isEmpty = function() {
      return!g.length
    };
    var n = function(a) {
      return function() {
        for(var b = [], d = g.length;d--;) {
          g[d][a](b)
        }
        return b
      }
    };
    this._keys = n("keys");
    this._values = n("values");
    this._entries = n("getEntries");
    this.values = function() {
      for(var a = this._values(), b = a.length, d = Kotlin.$new(Kotlin.ArrayList)();b--;) {
        d.add(a[b])
      }
      return d
    };
    this.remove = function(a) {
      l(a);
      var b = m(a), d = null, c = f(h, b);
      if(c && (d = c.removeEntryForKey(a), null !== d && !c.entries.length)) {
        a: {
          for(a = g.length;a--;) {
            if(c = g[a], b === c[0]) {
              break a
            }
          }
          a = null
        }
        j(g, a);
        delete h[b]
      }
      return d
    };
    this.size = function() {
      for(var a = 0, b = g.length;b--;) {
        a += g[b].entries.length
      }
      return a
    };
    this.each = function(a) {
      for(var b = c._entries(), d = b.length, e;d--;) {
        e = b[d], a(e[0], e[1])
      }
    };
    this.putAll = function(a, b) {
      for(var d = a._entries(), e, g, f, h = d.length, j = typeof b == i;h--;) {
        e = d[h];
        g = e[0];
        e = e[1];
        if(j && (f = c.get(g))) {
          e = b(g, f, e)
        }
        c.put(g, e)
      }
    };
    this.clone = function() {
      var a = new r(b, d);
      a.putAll(c);
      return a
    };
    this.keySet = function() {
      for(var a = Kotlin.$new(Kotlin.ComplexHashSet)(), b = this._keys(), d = b.length;d--;) {
        a.add(b[d])
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
  var e = Kotlin.$createClass(Kotlin.Iterator, {initialize:function(b, c) {
    this.map = b;
    this.keys = c;
    this.size = c.length;
    this.index = 0
  }, next:function() {
    return this.map[this.keys[this.index++]]
  }, hasNext:function() {
    return this.index < this.size
  }}), h = Kotlin.$createClass(Kotlin.Collection, {initialize:function(b) {
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
    var c = this.map, a;
    for(a in c) {
      if(c.hasOwnProperty(a) && c[a] === b) {
        return!0
      }
    }
    return!1
  }, get:function(b) {
    return this.map[b]
  }, put:function(b, c) {
    var a = this.map[b];
    this.map[b] = void 0 === c ? null : c;
    void 0 === a && this.$size++;
    return a
  }, remove:function(b) {
    var c = this.map[b];
    void 0 !== c && (delete this.map[b], this.$size--);
    return c
  }, clear:function() {
    this.$size = 0;
    this.map = {}
  }, putAll:function(b) {
    var b = b.map, c;
    for(c in b) {
      b.hasOwnProperty(c) && (this.map[c] = b[c], this.$size++)
    }
  }, keySet:function() {
    var b = Kotlin.$new(Kotlin.PrimitiveHashSet)(), c = this.map, a;
    for(a in c) {
      c.hasOwnProperty(a) && b.add(a)
    }
    return b
  }, values:function() {
    return Kotlin.$new(h)(this)
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
  var h = this.map[e];
  this.map[e] = !0;
  if(!0 === h) {
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
  function e(h, b) {
    var c = new Kotlin.HashTable(h, b);
    this.add = function(a) {
      c.put(a, !0)
    };
    this.addAll = function(a) {
      for(var b = a.length;b--;) {
        c.put(a[b], !0)
      }
    };
    this.values = function() {
      return c._keys()
    };
    this.iterator = function() {
      return Kotlin.arrayIterator(this.values())
    };
    this.remove = function(a) {
      return c.remove(a) ? a : null
    };
    this.contains = function(a) {
      return c.containsKey(a)
    };
    this.clear = function() {
      c.clear()
    };
    this.size = function() {
      return c.size()
    };
    this.isEmpty = function() {
      return c.isEmpty()
    };
    this.clone = function() {
      var a = new e(h, b);
      a.addAll(c.keys());
      return a
    };
    this.equals = function(a) {
      if(null === a || void 0 === a) {
        return!1
      }
      if(this.size() === a.size()) {
        for(var b = this.iterator(), a = a.iterator();;) {
          var c = b.hasNext(), e = a.hasNext();
          if(c != e) {
            break
          }
          if(e) {
            if(c = b.next(), e = a.next(), !Kotlin.equals(c, e)) {
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
      for(var a = "[", b = this.iterator(), c = !0;b.hasNext();) {
        c ? c = !1 : a += ", ", a += b.next()
      }
      return a + "]"
    };
    this.intersection = function(a) {
      for(var d = new e(h, b), a = a.values(), g = a.length, f;g--;) {
        f = a[g], c.containsKey(f) && d.add(f)
      }
      return d
    };
    this.union = function(a) {
      for(var b = this.clone(), a = a.values(), e = a.length, f;e--;) {
        f = a[e], c.containsKey(f) || b.add(f)
      }
      return b
    };
    this.isSubsetOf = function(a) {
      for(var b = c.keys(), e = b.length;e--;) {
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

