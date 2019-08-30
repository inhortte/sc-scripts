s.boot;

s.plotTree;

q = { Pan2.ar(WhiteNoise.ar(Line.kr(0.2, 0, 2)), Line.kr(-1, 1, 2, doneAction: Done.freeSelf)) }.play;
q.free;

(
  SynthDef(\pluckDeath, {
    arg freq = 440, amp;
    var quart = freq / 4;
    var pluck = 
      Pluck.ar(
        WhiteNoise.ar(amp).dup(2),
        Env.perc.kr(2),
        quart.reciprocal, freq.reciprocal, -1, Rand(0.01, 0.2), mul: 1
      );
    var filter = HPF.ar(
      in: pluck,
      freq: Line.kr(freq, freq * 2, 1, doneAction: Done.freeSelf)
    );
    Out.ar(0,
      Pan2.ar(
        filter,
        [ -0.5, 0.5 ]
        // Array.fill(2, { Rand(-1.0, 1.0) })
      )
    )
  }).add;
)
Synth(\pluckDeath, [\amp, 1, \freq, 220]);
(
  var seq = [ 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1318, 2960, 1396, 1396, 
    2960, 1318, 1318, 2960, 1318, 1318, 2960, 1318, 1396, 2960, 1396, 1396, 
  ];
  Pbind(
    \instrument, \pluckDeath,
    \dur, Pseq([0.333333, 0.333333, 1.333333], inf),
    \amp, Pseq([0, 0.5, 0.4], inf),
    \freq, Pseq(seq, 1),
  ).play(TempoClock(72/60));
)
