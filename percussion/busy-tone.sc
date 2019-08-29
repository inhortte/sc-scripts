(
  s.options.numBuffers = 8192; 
  s.boot;
)

(
  SynthDef(\busyTone, {
    var env = Env.new(levels: [0, 0.7, 0.5, 0], times: [0.04, 0.4166667, 0.2], curve: [-5, 0, 2]);
    var osc = SinOsc.ar([ 277, 1318 ], 0, EnvGen.kr(env, doneAction: Done.freeSelf) ! 2);
    var filter = LPF.ar(in: Mix(osc * [0.4, 0.15]), freq: Line.kr(start: 1500, end: 100, dur: 0.4, doneAction: 2));
    Out.ar(0,
      Pan2.ar(filter, 0)
    );
  }).add;
)

(
  Pbind(
    \instrument, \busyTone,
    \dur, 2
  ).play(TempoClock(72/60));
)
