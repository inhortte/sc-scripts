(
  SynthDef(\sinBastard, {
    arg outBus = 0, dur = 1, freq = 220, amp = 0.1;
    var env = EnvGen.ar(
      Env.new([0, 0.6, 1, 0], [dur * 0.3, dur * 0.5, dur * 0.2], curve: [-2, -1, 3]),
      doneAction: Done.freeSelf
    );
    var sin = SinOscFB.ar(
      freq: [freq * 0.99, freq, freq * 1.01],
      feedback: SinOsc.kr(dur * 2).range(pi / 6, pi / 3),
      mul: env * amp
    );
    Out.ar(outBus, Splay.ar(sin));
  }).add;
)
Synth(\sinBastard, [ freq: 3520, amp: 0.1, dur: 1 ]);
(
  SynthDef(\sawbastard, {
    arg outBus = 0, dur = 1, freq = 220, amp = 0.1, clip = 0.1;
    var env = Env.perc(0.04, dur * clip, curve: -3).kr(2);
    var saw = Saw.ar(
      freq: [freq * 0.98, freq, freq * 1.02]
    ) * env * amp;
    var lpf = LPF.ar(
      in: saw,
      freq: 880
    );
    Out.ar(outBus, Splay.ar(lpf));
  }).add;
)
Synth(\sawbastard, [ freq: 740, amp: 0.5, dur: 0.5, clip: 0.8 ]);
(
  SynthDef(\pops, {
    arg outBus = 0, freq = 3520, amp = 0.1, dur = 1, clip = 0.1;
    var env = Env.perc(0.001, dur * clip).kr(2);
    var noise = PinkNoise.ar(amp) * env;
    var filter = HPF.ar(
      in: noise,
      freq: [freq * 0.5, freq, freq * 1.8]
    );
    Out.ar(outBus, filter);
  }).add;
)
Synth(\pops, [ freq: 3520, amp: 0.5, dur: 1 ]);
(
  SynthDef(\lowPass, {
    arg inBus = 8, outBus = 0;
    var lpf = RLPF.ar(
      in: In.ar(inBus, 2),
      freq: SinOsc.kr(0.25).range(1760, 2794),
      rq: 0.1
    );
    Out.ar(outBus, lpf);
  }).add;
)
(
  var pBus = Bus.audio(s, 2);
  var luko = Routine {
    loop({
      if (
        0.2.coin, 
        { 4435.yield; }
      );
      if (
        0.2.coin, 
        { 5587.yield; }
      );
      3520.yield;
    });
  };
  g = Group.basicNew(s, 1);
  Routine({
    loop({
      Synth.head(g, \sinBastard, [ freq: luko.next;, amp: 0.1, dur: 2, outBus: pBus ]);
      3.wait;
    });
  }).play;
  Synth.tail(g, \lowPass, [ inBus: pBus, outBus: 0 ]);
)
(
  var amps = [ 0.5, 0.48, 0.52, 0.47, 0.54, 0.41 ];
  var clips = [ 0.1, 0.08, 0.07, 0.12, 0.13, 0.06, 0.07, 0.05, 0.06, 0.1, 0.09 ];
  Pbind(
    \instrument, \pops,
    \amp, Prand(amps, inf),
    \dur, 2,
    \clip, Prand(clips, inf)
  ).play;
  Pbind(
    \instrument, \pops,
    \amp, Prand(amps, inf),
    \dur, Pseq([0.5, 1.55], inf),
    \clip, Prand(clips, inf)
  ).play;
  Pbind(
    \instrument, \sawbastard,
    \amp, 0.4,
    \dur, Pseq([0.5, 1.55], inf),
    \freq, Pseq([740, 831], inf),
    \clip, Prand([0.8, 0.5, 0.6, 0.7], inf)
  ).play;
)
