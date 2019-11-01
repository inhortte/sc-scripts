(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\uncertainTuning, {
    arg outBus = 0, freq = 220, vibrato1 = 0.3, vibrato2 = 0.4, dur = 1, amp = 0.1;
    var env = EnvGen.ar(
      Env.perc(0.001, dur * 0.92, amp, -2.0),
      gate: 1.0,
      doneAction: Done.freeSelf
    );
    var source = Saw.ar(
      freq: [ SinOsc.kr(vibrato1, pi / 2) * freq.log2 + freq,
              SinOsc.kr(vibrato2, pi / 3) * freq.log2 + freq ]
       // freq: [freq, freq]
    ) * env;
    var fx = DelayC.ar(
      in: source,
      maxdelaytime: 0.01,
      delaytime: SinOsc.ar(Rand(5, 10), 0, 0.0025, 0.0075)
    );
    Out.ar(outBus, fx);
  }).add;
)
Synth(\uncertainTuning, [ freq: 220, vibrato1: 1, vibrato2: 1.2, dur: 2, amp: 0.5 ]);

(
  {
    z = Decay.ar(Dust.ar(12,0.5), 0.3, WhiteNoise.ar) ! 2;
    DelayC.ar(z, 0.2, 0.2, 1, z); // input is mixed with delay via the add input
  }.play
)

(
  SynthDef(\anotherChorus, {
    arg outBus = 0, freq = 220, dur = 1, amp = 0.1, multiplier = 10;
    var env = EnvGen.ar(
      Env.new(
        levels: [0, 1, 0],
        times: [0.1, dur * 0.9],
        curve: \lin
      ),
      gate: Impulse.kr(dur.reciprocal)
    ) ! 2;
    var saw = Saw.ar(freq, amp) * env;
    var chorus = Mix.fill(multiplier, {
      var maxdelaytime = Rand(0.01, 0.03);
      DelayC.ar(
        in: saw,
        maxdelaytime: maxdelaytime,
        delaytime: LFNoise1.kr(Rand(5, 10), 0.01, 0.02)
      );
    });
    Out.ar(outBus, Splay.ar(chorus));
  }).add;
)
Synth(\anotherChorus, [ freq: 220, dur: 2, amp: 0.2 ]);


