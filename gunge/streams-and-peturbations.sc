(
  s.options.numBuffers = 8192; 
  s.options.numWireBufs = 1024;
  s.boot;
)
s.plotTree;

(
  var borderliners = FuncStream.new({
    #[1, 2, 3, 4].choose;
  });
  5.do({
    borderliners.next.postln;
  });
)
(
  var pinch = Routine.new({
    4.do({
      arg i;
      i.yield;
    });
  });
  5.do({
    pinch.next.postln;
  });
)
(
  var zeroToNine = Routine.new({
    10.do({ arg i; i.yield; });
  });
  var juicy = zeroToNine.squared;
  var pointy = zeroToNine + 99;
  12.do({ juicy.next.postln; });
  // juicy and pointy are references to zeroToNine. The following line will only eruct nils
  12.do({ pointy.next.postln; });
)
(
  var zeroToNine = Routine.new({ 10.do({ arg i; i.yield; }); });
  var bungle = Routine.new({ 
    forBy (100, 280, 20, {
      arg i;
      i.yield;
    });
  });
  var stemming = zeroToNine + bungle;
  12.do({ stemming.next.postln; });
)
(
  var triangle = Routine.new({
    15.do({
      arg i;
      var n = i * (i + 1) / 2;
      n.yield;
    });
  });
  var evens = triangle.collect({
    arg n;
    if(n % 2 == 0, { "thurk" }, { n });
  });
  15.do({ evens.next.postln; });
)
(
  var tri = Routine.new({
    15.do({
      arg i;
      var n = i * (i + 1) / 2;
      n.yield;
    });
  });
  var evens = tri.select({
    arg n;
    n % 2 == 0;
  });
  15.do({ evens.next.postln; });
)

