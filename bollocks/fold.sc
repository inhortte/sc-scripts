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
      level: 1,
      curve: 0.8
    ).kr(2);
    /*
    var env = Linen.kr(
      gate: 1.0,
      attackTime: 0.04,
      susLevel: amp,
      releaseTime: 0.3,
      doneAction: Done.freeSelf
    );
    */
    var tri = LFTri.ar(
      freq: freq,
      iphase: [0, 1.3, 2.9, 3.2],
      mul: amp
    ) * env;
    var lpf = HPF.ar(
      in: tri,
      freq: Line.kr(freq * 7, freq / 2, dur * 0.8),
      // freq: SinOsc.ar(XLine.kr(freq * 7, freq / 2, dur * 0.8), 0, 0.5).range(freq * 7, freq / 2),
      // freq: XLine.kr(freq / 2, freq * 2, dur * 0.8, doneAction: Done.freeSelf),
    );
    Out.ar(
      out,
      Splay.ar(lpf)
    );
  }).add;
)
Synth(\trianglish, [ freq: 220, dur: 5 ]);
{ SinOsc.kr(4, pi / 2) }.scope;
(
  var clock = TempoClock(90/60);
  var tremolo = SinOsc.kr(6, pi / 2, mul: 0.5, add: 0.5);
  var tBus = Bus.audio(s, 2);
  var thurk;
  {
    Pbind(
      \instrument, \trianglish,
      \out, tBus,
      \amp, Pseq([0.5, 0.3, 0.5, 0.3, 0.5, 0.3, 0.3, 0.3, 0.3, 0.5, 0.3, 0.3, 0.3, 0.3, 0.5, 0.3, 0.5, 0.3], 2),
      \scale, Scale.mixolydian,
      \degree, Pseq([0, 5, 3, 0, 5, 3, 0, 5, 3, 0, 5, 3, 0, 5, 3, 0, 5, 3], 2),
      \dur, 0.5
    ).play(clock);
    thurk = HPF.ar(
      in: In.ar(tBus, 2),
      freq: 1024,
      mul: tremolo
    );
    Out.ar(0, thurk);
  }.play;
)

Scale.directory;
(
  b = Scale.mixolydian;
)

