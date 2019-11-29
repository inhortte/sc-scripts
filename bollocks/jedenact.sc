(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;
(
  SynthDef(\sawMyFaceOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var quarterUp = freq * 2 - freq * ( 1 / 24 ) + freq;
    var freqEnv = EnvGen.ar(
      Env.new([freq, freq, quarterUp, freq], [dur * 0.25, dur * 0.25, dur * 0.25], curve: [0, -2, -1])
    );
    var env = Env.perc(0.01, dur, curve: 2).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freqEnv,
      iphase: 1.2
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: 100,
      rq: 0.4
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, lpf ! 2);
  }).add;
)
(
  SynthDef(\sawMyThoraxOff, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var quarterUp = freq * 2 - freq * ( 1 / 24 ) + freq;
    var freqEnv = EnvGen.ar(
      Env.new([freq, freq, quarterUp, freq], [dur * 0.2, dur * 0.2, dur * 0.2], curve: [0, -2, -1])
    );
    var env = Env.perc(0.01, dur * (3/5), curve: 2).kr(2) * amp;
    var saw = LFSaw.ar(
      freq: freqEnv,
      iphase: 1.2
    ) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: 100,
      rq: 0.4
    );
    var allPass = AllpassN.ar(lpf, 0.1, [ 0.09.rand, 0.08.rand ], 4);
    Out.ar(0, lpf ! 2);
  }).add;
)
Synth(\sawMyFaceOff, [ freq: 62, dur: 5, amp: 0.2 ]);
(
  var freqs = [
    62, 83, 87,
    62, 83, 87, 98, // b e f g
    62, 83, 87,
    62, 83, 87, 98, // b e f g
    62, 83, 87,
    62, 83, 87, 98, // b e f g
    62, 83, 87,
    62, 83, 87, 98, // b e f g
    62, 83, 87,
    62, 83, 87, 98, // b e f g
  ];
  var durs = [
    1.5, 0.5, 3,
    1.5, 0.5, 1, 2,
    1.5, 0.5, 3,
    1.5, 0.5, 1, 2,
    1.5, 0.5, 3,
    1.5, 0.5, 1, 2,
    1.5, 0.5, 3,
    1.5, 0.5, 1, 2,
    1.5, 0.5, 3,
    1.5, 0.5, 1, 2,
  ];
  Pbind(
    \instrument, \sawMyFaceOff,
    \dur, Pseq(durs, 1),
    \freq, Pseq(freqs, 1),
    \amp, Prand([0.2, 0.22, 0.18, 0.19], inf)
  ).play(TempoClock(72/60));
)
(
  Routine({
    loop({
      Synth(\sawMyThoraxOff, [ freq: 55, dur: 10, amp: 0.2 ]);
      8.3333333333.wait;
    });
  }).play;
)
