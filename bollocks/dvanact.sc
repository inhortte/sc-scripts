(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
(
  SynthDef(\dusty, {
    arg outBus = 0, dur = 1, amp = 0.1, density = 64;
    var env = Env.perc(0.3, dur * 1.5).kr(2);
    var dust = Dust.ar(
      density: [ density, density * 0.8, density * 1.2 ],
      mul: amp
    ) * env;
    Out.ar(outBus, Splay.ar(dust));
  }).add;
)
Synth(\dusty, [ dur: 2, amp: 0.5, density: 16000 ]);
(
  Array.fill(24, {
    arg n;
    1 - (1 / (24 - n))
  }).postln;
)
Array.geom(24, 1, 1.5).postln;
(
  var dArr = Array.geom(24, 1, 1.5);
  var size = dArr.size;
  var densities = Pseq(dArr, 1).asStream;
  var durArr = Array.geom(24, 1, 0.9);
  var durs = Pseq(durArr, 1).asStream;
  Task({
    size.do({
      var dur = durs.next;
      Synth(\dusty, [ dur: dur, amp: 0.5, density: densities.next ]);
      dur.wait;
    });
  }).play;
)
(
  SynthDef(\sawMyFaceOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var halfUp = freq * 2 - freq * ( 1 / 12 ) + freq;
    var freqEnv = EnvGen.ar(
      Env.new([freq, halfUp, freq, halfUp], [dur * 0.25, dur * 0.25, dur * 0.25], curve: [0, -2, -1])
    );
    var env = Env.perc(0.01, dur, curve: 2).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freqEnv,
      iphase: 1.2
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: SinOsc.kr(2).range(100, 150),
      rq: 0.2
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, lpf ! 2);
  }).add;
)
(
  SynthDef(\sawMyThoraxOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var eighthUp = freq * 2 - freq * ( 1 / 48 ) + freq;
    var freqEnv = EnvGen.ar(
      Env.new([freq, eighthUp, freq, eighthUp], [dur * 0.25, dur * 0.25, dur * 0.25], curve: [0, -2, -1])
    );
    var env = Env.perc(0.01, dur * 0.8, curve: 2).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freqEnv,
      iphase: 1.2
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: SinOsc.kr(0.4).range(100, 350),
      rq: 0.2
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, lpf ! 2);
  }).add;
)
Synth(\sawMyFaceOff, [ freq: 49, dur: 2, amp: 0.2 ]);
(
  var freqs = [ 77.78, 82.41, 52, \ ];
  var durs = [ 0.25, 0.25, 1.5, 2 ];
  Pbind(
    \instrument, \sawMyFaceOff,
    \amp, Prand([0.2, 0.18, 0.17, 0.19], inf),
    \freq, Pseq(freqs, inf),
    \dur, Pseq(durs, inf)
  ).play;
)
(
  var freqs = [
    82.41, 87.31, 61.74, 65.41, \,
    82.41, 87.31, 61.74, 65.41, \,
    82.41, 87.31, 61.74, 65.41, \,
    82.41, 87.31, 61.74, 65.41, \,
    51.91, 55, 39, 41, \,
    51.91, 55, 39, 41, \,
    51.91, 55, 39, 41, \,
    51.91, 55, 39, 41, \,
    77.78, 73.42, 58, 55, \,
    77.78, 73.42, 58, 55, \,
    77.78, 73.42, 58, 55, \,
    77.78, 73.42, 58, 55, \,
  ];
  var durs = [ 0.25, 0.75, 0.25, 0.75, 1 ];
  Pbind(
    \instrument, \sawMyThoraxOff,
    \freq, Pseq(freqs, inf),
    \dur, Pseq(durs, inf),
    \amp, Prand([0.2, 0.19, 0.18, 0.17], inf)
  ).play;
)
