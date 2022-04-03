(
  s.plotTree;
  s.meter;
  s.scope;
)
(
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;
    g = Group.basicNew(s, 1);
    d = Bus.audio(s, 2);
    
    SynthDef(\UZSaw, {
      var amp = \amp.ir(0.3);
      var freq = \freq.ir(220);
      var dur = \dur.ir(1.0);
      var sawEnv = EnvGen.kr(
        Env(
          levels: [0.0, 1, 0, 0],
          times: [0.97 * dur, 0.02 * dur, 0.01 * dur],
          curve: [2, -1, 0]
        ),
        gate: 1,
        doneAction: 2
      );
      var sig = LFSaw.ar(
        freq: freq,
        iphase: [ 0.3, 0.9 ]
      ) * sawEnv * amp;
      sig = RLPF.ar(
        in: sig,
        freq: SinOsc.kr(
          SinOsc.kr(0.2).range(0.4, 0.6)
        ).range(freq * 3, freq * 4),
        rq: 0.1
      );
      Out.ar(\outBus.ir(0), Splay.ar(sig));
    }).add;

    SynthDef(\UZPulse, {
      var amp = \amp.ir(0.3);
      var freq = \freq.ir(220);
      var dur = \dur.ir(1.0);
      var dustEnv = EnvGen.kr(
        Env.perc(0.001, 0.04, curve: -3),
        gate: Dust2.ar(
          SinOsc.kr(
            SinOsc.kr(0.4).range(0.3, 0.7)
          ).range(512, 1536)
        )
      ) * 0.6 + 0.4;
      var ampEnv = EnvGen.kr(
        Env(
          levels: [0, 0.2, 1, 0],
          times: [0.02 * dur, 0.93 * dur, 0.05 * dur],
          curve: [1.5, 1]
        ),
        gate: 1,
        doneAction: 2
      );
      var cutoffEnv = EnvGen.kr(
        Env(
          levels: [freq * 3, freq * 15, freq * 17],
          times: [0.95 * dur, 0.05 * dur],
          curve: [1, -2]
        ),
        gate: 1
      );
      var sig = LFPulse.ar(
        freq: freq,
        iphase: [ 0.0, 0.25, 0.9, 0.68 ],
        width: SinOsc.kr(
          LFTri.kr(0.5).range(0.4, 1.1)
        ).range(-0.9, -0.1)
      ) * ampEnv * amp * dustEnv;
      sig = RLPF.ar(
        in: sig,
        freq: SinOsc.kr(
          SinOsc.kr(0.4).range(0.2, 0.6)
        ).range(freq * \lowCO.ir(4), freq * \highCO.ir(9)),
        rq: 0.1
      );
      sig = LPF.ar(
        in: sig,
        freq: cutoffEnv
      );
      Out.ar(\outBus.ir(0), Splay.ar(sig));
    }).add;

    SynthDef(\reverberation, {
      var sig, wet;
      sig = In.ar(\inBus.ir(0), 2);
      sig = FreeVerb2.ar(sig[0], sig[1], \mix.ir(0.4), \room.ir(0.60), \damp.ir(0.2));
      sig = LPF.ar(sig, [SinOsc.kr(0.05).range(880, 1220), 1174]);
      // sig = LPF.ar(sig, \cutoff.ir(1174));  // D6
      Out.ar(\outBus.ir(0), Splay.ar(sig));
    }).add;

    ~d7melody1 = [ 39, 42, 39, 43 ]; // ees fis ees g 
    ~edormelody1 = [ 39, 42, 39, 44 ]; // ees ges ees aes
    ~d7phr1 = Pbind(
      \instrument, \UZPulse,
      \group, g,
      \outBus, d,
      \addAction, 0,
      \amp, 0.4,
      \dur, Pseq([1.5, 1.5, 1, 1], inf),
      \freq, Pseq(~d7melody1.midicps, inf)
    );
    ~edor1 = Pbind(
      \instrument, \UZPulse,
      \group, g,
      \outBus, d,
      \addAction, 0,
      \amp, 0.4,
      \highCO, 15,
      \dur, Pseq([1.5, 1.5, 1, 1], inf),
      \freq, Pseq(~edormelody1.midicps, inf)
    );
  });
)

Synth.tail(g, \reverberation, [ outBus: 0, inBus: d, mix: 0.5, room: 0.7 ]);
(
  Ppar([
    // ~d7phr1
    ~edor1
  ]).play(TempoClock(54/60));
)