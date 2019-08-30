(
  s.options.numBuffers = 8192; 
  s.boot;
)

(
  SynthDef(\busyTone, {
    var env = Env.new(levels: [0, 0.7, 0.5, 0, 0, 0.7, 0.5, 0], times: [0.04, 0.3, 0.1, 0.233333, 0.04, 0.3, 0.2], curve: [-5, 0, 2, 0, -5, 0, 2]);
    var osc = SinOsc.ar([ 277, 1318 ], 0, EnvGen.kr(env, doneAction: Done.freeSelf) ! 2);
    var filter = LPF.ar(in: Mix(osc * [0.4, 0.15]), freq: Line.kr(start: 1500, end: 100, dur: 0.4, doneAction: 2));
    Out.ar(0,
      Pan2.ar(filter, 0)
    );
  }).add;
  SynthDef(\shortBusy, {
    var env = Env.new(levels: [0, 0.7, 0.5, 0], times: [0.04, 0.3, 0.2], curve: [-5, 0, 2]);
    var osc = SinOsc.ar([ 277, 1318 ], 0, EnvGen.kr(env, doneAction: Done.freeSelf) ! 2);
    var filter = LPF.ar(in: Mix(osc * [0.4, 0.15]), freq: Line.kr(start: 1500, end: 100, dur: 0.333333, doneAction: 2));
    Out.ar(0,
      Pan2.ar(filter, 0)
    );
  }).add;
)

(
  Pbind(
    \instrument, \shortBusy,
    \dur, Pseq([0.66666, 1.333333], inf),
  ).play(TempoClock(72/60));
)
