s.boot;

(
  SynthDef('kickDrum', {
    var osc, env, output;
    osc = SinOsc.ar(60);
    env = Line.ar(Rand(0.4, 0.6), 0, Rand(0.5, 0.7), doneAction: 2);
    output = osc * env;
    Out.ar(0, Pan2.ar(output, 0));
  }).send(s);
)
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
