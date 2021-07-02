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

(
  SynthDef(\someBitch, {
    arg outBus = 0, freq = 220, amp = 0.1, dur = 1;
    var env = EnvGen.kr(
      Env.perc(0.01, dur, curve: -3),
      levelScale: amp,
      doneAction: Done.freeSelf
    );
    var saw = LFSaw.ar(
      freq: freq,
      mul: env
    );
    var lpf = RLPF.ar(
      in: saw,
      freq: LFNoise1.kr(2, 24, 89).midicps,
      rq: 0.1
    );
    var out = lpf;
    6.do({ out = AllpassN.ar(out, 0.05, [0.05.rand, 0.05.rand], 4) });
    Out.ar(outBus, out);
  }).add;
)
Synth(\someBitch, [ freq: 220, amp: 0.1, dur: 2 ]);
(
  var gQu = Pseq([
    Prand([
      nil,
      Pseq(#[79, 84, 89, 82, 87]);
    ]),
    Pseq([55, Prand(#[60, 101]), 58, Prand(#[94, 63])], { rrand(2, 5) }),
    Prand(#[79, 84, 89], { rrand(3, 9) })
  ], inf).asStream.midicps;
  var fisQu = Prand([ 90, 83, 100, 81, 98 ], inf).asStream.midicps;
  Task({
    loop({
      Synth(\someBitch, [ freq: fisQu.next, dur: 0.24691, amp: 0.1 ]);
      0.24691.wait;
    });
  }).play;
)
{ Decay2.ar(Impulse.ar(0.1), 0.002, 0.01) }.plot;
(
  SynthDef(\percMadness, {
    arg outBus = 0, tAmp = 0.1, amp = 0.1, dur = 1;
    var noise = WhiteNoise.ar(70) * Env.perc(0.01, dur * 0.8, curve: -5).kr(2);
    var sound = Resonz.ar(
      in: noise,
      freq: 34.midicps, // bes
      bwr: SinOsc.kr(1.48).range(0.01, 0.04),
      mul: 4 // 4 in original
    ).distort * amp;
    Out.ar(outBus, sound ! 2);
  }).add;
)
Synth(\percMadness, [ tAmp: 0.1, amp: 0.1, dur: 1 ]);
(
  var pattern = Prand([
    Pseq(#[0.9, 0.2, 0.5, 0.2]),
    Pseq(#[0.8, 0.3, 0.5, 0.2]),
    Pseq(#[0.8, 0.2, 0.4, 0.1]),
    Pseq(#[0.7, 0.3, 0.5, 0.1]),
    Pseq(#[0.6, 0.4, 0.5, 0.3])
  ], 10).asStream;
  Task({
    var amp;
    loop({
      if((amp = pattern.next) > 0) {
        Synth(\percMadness, [ tAmp: amp, amp: 0.1, dur: 0.37037 ]);
      };
      0.37037.wait;
    });
  }).play;
)


(
  {
    LFSaw.ar(
      freq: 196,
      phase: 0.0,
      mul: 0.1
    )
  }.play;
)


