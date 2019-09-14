(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\trianglish, { | out = 0, freq = 147, dur = 1, amp = 0.1 |
    var env = Env.perc(
      attackTime: 0.04,
      releaseTime: dur * 0.8,
      level: amp,
      curve: 0.8
    ).kr(2);
    var tri = LFTri.ar(
      freq: freq,
      iphase: [0, 1.3, 2.9, 3.2]
    ) * env;
    var lpf = LPF.ar(
      in: tri,
      freq: XLine.kr(freq * 7, freq / 2, dur * 0.8, doneAction: Done.freeSelf)
    );
    Out.ar(
      out,
      Splay.ar(lpf)
    );
  }).add;
)
Synth(\trianglish);
(
  var clock = TempoClock(90/60);
  Pbind(
    \instrument, \trianglish,
    \out, 0,
    \amp, Pseq([0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.3, 0.3, 0.3, 0.5, 0.3, 0.3, 0.3, 0.3, 0.5, 0.3, 0.5, 0.3], 2),
    \scale, Scale.mixolydian,
    \degree, Pseq([0, 5, 3, 0, 5, 3, 0, 5, 3, 0, 5, 3, 0, 5, 3, 0, 5, 3], 2),
    \dur, 0.5
  ).play(clock);
)

Scale.directory;
(
  b = Scale.mixolydian;
)

