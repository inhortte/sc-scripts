(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\metronom, {
    arg outBus = 0, dur = 1, amp = 0.1;
    var env = Env.perc(0.01, dur * 0.05, curve: -5).kr(2);
    var noise = PinkNoise.ar(amp) * env;
    Out.ar(outBus, noise ! 2);
  }).add;
)

(
  Pbind(
    \instrument, \metronom,
    \dur, 2,
    \amp, 0.5
  ).play(TempoClock(84/60));
)
