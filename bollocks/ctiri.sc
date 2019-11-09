(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\pluck, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1;
    var env = Env.perc(0.01, dur * 0.3, curve: -4).kr(2);
    var pluck = Pluck.ar(
      WhiteNoise.ar(amp),
      1,
      freq.reciprocal,
      freq.reciprocal,
      -2,
      SinOsc.kr(0.7143).range(-0.5, 0.5)
    ) * env;
    Out.ar(outBus, pluck ! 2);
  }).add;
)
(
  SynthDef(\puckFilter, {
    arg inBus = 7, outBus = 0;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(84/60/3, 0.5pi).range(277, 830),
      rq: 0.2
    );
    Out.ar(outBus, lpf);
  }).add;
)
Synth(\pluck, [ freq: 220, dur: 1, amp: 0.1 ]);
(
  var clock = TempoClock(84/60);
  var freqs = [ 208, 196, 175, 208, 196 ];
  var durs = [ 0.5, 1, 0.5, 0.5, 1 ];
  var pBus = Bus.audio(s, 2);
  g = Group.basicNew(s, 1);
  Pbind(
    \instrument, \pluck,
    \group, g,
    \addAction, 0,
    \outBus, pBus,
    \dur, Pseq(durs, inf),
    \freq, Pseq(freqs, inf),
    \amp, 0.2
  ).play(clock);
  Synth.tail(g, \puckFilter, [ inBus: pBus, outBus: 0 ]);
)
