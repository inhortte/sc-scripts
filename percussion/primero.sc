s.boot;

(
  var tempo, harmonics, blood;
  tempo = TempoClock(72/60);
  harmonics = (1..5);
  harmonics.postln;
  SynthDef(\kickDrumTone, {
    arg freq = 58, level = 0.8;
    var osc, filter, kickEnv = Env.perc(attackTime: 0.05, releaseTime: 1.5, level: level * (1 / (harmonics + 1)), curve: 0.3).kr(2);
    osc = Saw.ar(freq * harmonics, kickEnv);
    filter = BPF.ar(
      in: osc,
      freq: Line.kr(freq * 4, freq / 2, 1.5, doneAction: 2),
      rq: 0.1,
      mul: Env.perc(attackTime: 0.25, releaseTime: 1.5, level: 1, curve: 0).kr(2)
    );
    Out.ar(0, Pan2.ar(filter, 0));
  }).add;

  SynthDef(\kickDrumNoise, {
    arg level = 0.6;
    var osc, env, filter;
    env = Env.perc(attackTime: 0.01, releaseTime: 0.75, level: level, curve: 0.45).kr(2);
    osc = LPF.ar(WhiteNoise.ar(env), Rand(500, 800));
    filter = LPF.ar(
      in: osc,
      freq: Line.kr(Rand(600, 1000), 200, 0.75, doneAction: 2)
    );
    Out.ar(0, Pan2.ar(filter, 0));
  }).add;

  blood = Pseq([1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0], inf);
  Pbind(
    \instrument, \kickDrumTone,
    \freq, 55,
    \level, blood,
    \dur, 1
  ).play(tempo);
  Pbind(
    \instrument, \kickDrumNoise,
    \level, 0.6 * blood,
    \dur, 1
  ).play(tempo);
)

{PinkNoise.ar(Env.perc(attackTime: 2, releaseTime: 2, level: 0.5, curve: 1).kr(2))}.play;
{SinOsc.ar(58, 0, Env.perc(attackTime: 2, releaseTime: 2, level: 0.5, curve: 1).kr(2))}.play;

(
  SynthDef('kickClick', {
    var osc, env, output;
    osc = LPF.ar(WhiteNoise.ar(), Rand(500, 800));
    env = Line.ar(Rand(0.8, 0.9), 0, 0.01);
    output = osc * env;
    Out.ar(0, Pan2.ar(output, 0));
  }).send(s);
)

t = Synth('kickDrum');
u = Synth('kickClick');
Mix([Synth('kickDrum'), Synth('kickClick')])
t.free
