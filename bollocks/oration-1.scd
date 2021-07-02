(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  SynthDef(\highPerc, {
    arg outBus = 0, freq = 220, dur = 1, durMult = 0.8, amp = 0.1;
    var env = Env.perc(0.01, durMult * dur, curve: -3).kr(2);
    var noise = GrayNoise.ar(amp) * env;
    var klang = Klank.ar(
      `[
        [ freq, freq * 1.02, freq * 0.98 ],
        [ amp, amp * 0.9, amp * 0.8 ],
        [ dur * 0.2, dur * 0.18, dur * 0.22 ]
      ],
      noise
    ) * env;
    var bpf = BPF.ar(
      in: klang,
      freq: SinOsc.kr(0.2).range(240, 760),
      rq: 0.5
    );
    Out.ar(outBus, Splay.ar(bpf));
  }).add;
)
Synth(\highPerc, [ freq: 197.58, dur: 1, amp: 0.1 ]);
(
  SynthDef(\verbDelayFX, {
    arg inBus, out=0, revWet=0.0, dlyWet=0.3, dTime = 0.3, feedback = 0.5 ;
    
    var snd = In.ar(inBus, 2);
    var verb = JPverb.ar(snd);
    var delay = Greyhole.ar(snd, 
      delayTime: dTime,
      diff: 0.01,
      feedback: feedback
    );
    
    snd = snd + (verb * revWet) + (delay * dlyWet) * 0.5;
    Out.ar(out, snd);
  }).add;
)
(
  ~reverBus = Bus.audio(s,2);
  ~reverbDelay = Synth(\verbDelayFX, [\inBus, ~reverBus, \dTime, 0.185, \dlyWet, 0.0, \revWet, 0.7], addAction: \addAfter);
)

// 4/4
(
  var clock = TempoClock(96/60);
  Pbind(
    \instrument, \highPerc,
    \outBus, ~reverBus,
    \freq, Prand([174.61, 164.81, 130.81] * 2, inf),
    \dur, Pseq([0.5, 1.0, 0.5, 0.5, 1.5], inf),
    \durMult, Prand([0.2, 0.3, 0.1, 0.5], inf)
  ).play(clock);
)

// 12/8
(
  var clock = TempoClock(54/60);
  Pbind(
    \instrument, \highPerc,
     \outBus, ~reverBus,
    //\outBus, 0,
    \freq, Prand([174.61, 164.81, 130.81] * 2, inf),
    \dur, Pseq([0.6666666666666, 1.0, 0.3333333333333333, 0.3333333333333333, 0.6666666666666, 1.0], inf),
    \durMult, Prand([0.2, 0.3, 0.1, 0.5], inf)
  ).play(clock);
)

// 3/4
(
  var clock = TempoClock(54/60);
  Pbind(
    \instrument, \highPerc,
     \outBus, ~reverBus,
    \freq, Prand([174.61, 164.81, 130.81] * 2, inf),
    \dur, Pseq([0.5, 1.0, 0.5, 1.0], inf),
    \durMult, Prand([0.2, 0.3, 0.1, 0.5], inf)
  ).play(clock);
)
