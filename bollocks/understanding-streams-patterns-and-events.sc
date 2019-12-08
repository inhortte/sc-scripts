(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  var lepton, man;
  lepton = Pfunc.new({
    #[1, 2, 3, 4].choose;
  });
  man = lepton.asStream;
  5.do({
    man.next.postln;
  });
)
Env.new([0, 0.3, 1], [0.6, 0.3], [-3, 2]).plot;
XLine.kr(0.01, 0.3, 0.6).plot;
(
  SynthDef(\buddaF, {
    arg outBus = 0, freq = 87, dur = 1, amp = 0.1;
    var sawEnv = Env.perc(0.03, dur, curve: -1).kr(2);
    var noiseEnv = Env.perc(0.03, dur * 0.5, curve: -3).kr(0);
    var filterEnv = EnvGen.kr(
      Env.new([0.01, 0.3, 1], [dur * 0.6, dur * 0.3], [-3, 2]),
      doneAction: Done.none
    );
    var noise = GrayNoise.ar(amp * 0.5) * noiseEnv;
    var saw = LFSaw.ar(
      freq: freq,
      iphase: [0.5, 1, 1.5],
      mul: amp
    ) * sawEnv;
    var lpf = RLPF.ar(
      in: saw,
      freq: XLine.kr(freq * 2.8, freq * 32, 0.9 * dur),
      rq: 0.2
    );
    var out = Mix([noise, lpf]);
    4.do({ out = AllpassN.ar(out, 0.05, [0.05.rand, 0.05.rand], 4) });
    Out.ar(outBus, out);
  }).add;
)
Synth(\buddaF, [ freq: 44, dur: 6, amp: 0.2 ]);

(
  SynthDef(\leper, {
    arg outBus = 0, sustain = 1, freq = 87, amp = 0.1, filtMult = 1;
    var env = EnvGen.kr(
      Env.perc,
      levelScale: amp,
      timeScale: sustain,
      doneAction: Done.freeSelf
    );
    var saw = LFSaw.ar(freq) * env;
    var lpf = RLPF.ar(
      in: saw,
      freq: LFNoise1.kr(2.4, 24, 82).midicps,
      rq: 0.2
    );
    var out = LPF.ar(
      in: lpf,
      freq: SinOsc.kr(0.416666667, 0, 1).range(349, 466 * filtMult)
    );
    4.do({
      out = AllpassN.ar(out, 0.05, [0.05.rand, 0.05.rand], 4);
    });
    Out.ar(outBus, out);
  }).add;
)
Synth(\leper, [ freq: 87, amp: 0.1, sustain: 1 ]);
(
  SynthDef(\dusty, {
    arg outBus = 0, amp = 0.1, dur = 1;
    var pinkEnv = Env.perc(0.01, dur * 0.1, curve: -5).kr(0);
    var dustEnv = Env.perc(0.05, dur * 0.2, curve: -1).kr(2);
    var pink = PinkNoise.ar(amp) ! 2;
    var dust = Dust.ar({ #[16, 32, 64, 128].choose }, amp * 1.5) ! 2;
    var out = Mix([ pink * pinkEnv, dust * dustEnv ]);
    Out.ar(outBus, out);
  }).add;
)
Synth(\dusty, [ amp: 0.1, dur: 1 ]);
(
  var dur = 1/7;
  var durDiff = 3;
  var pattern1 = Prout.new({
    loop({
      if(0.2.coin, { 49.yield; });
      if(0.2.coin, { 48.yield; });
      if(0.2.coin, { 46.yield; });
      34.yield;
    });
  });
  var streams1 = [
    (pattern1 + Pfunc.new({ #[12, 7, 7, 0].choose })).midicps.asStream,
    pattern1.midicps.asStream
  ];
  var durs1 = [ 
    0.27777778, 0.27777778, 0.27777778, 
    0.27777778, 0.27777778, 0.27777778,
    0.4166667, 0.4166667,
    0.555555556, 0.27777778 + 1.6666667
  ];
  var durs2 = [
    0.4166667, 0.4166667,
    0.4166667, 0.4166667,
    0.27777778, 0.27777778, 0.27777778, 
    0.8333333,
    0.4166667, 0.4166667,
    0.27777778, 0.27777778, 0.27777778
  ];
  var durs3 = [
    0.27777778, 0.27777778, 0.27777778, 
    0.8333333,
    0.8333333,
    0.4166667, 0.4166667,
    0.4166667, 0.4166667,
    0.4166667, 0.4166667
  ];
  var amps1 = Array.fill(9, { #[0.2, 0.23, 0.21, 0.22].choose }) ++ [0.1];
  amps1.postln;
  /*
  Routine({
    loop({
      Synth(\leper, [ freq: streams.at(0).next, sustain: dur * durDiff ]);
      durDiff.do({
        Synth(\leper, [ freq: streams.at(1).next, sustain: dur ]);
        dur.wait;
      });
    });
  }).play;
  */
  Routine({
    loop({
      durs1.do({
        arg dur, idx;
        var freq = streams1.at(1).next;
        var amp = amps1.at(idx);
        Synth(\leper, [ freq: freq, sustain: dur, amp: amp ]);
        dur.wait;
      });
    });
  }).play;
  Routine({
    loop({
      Synth(\leper, [ freq: streams1.at(0).next, sustain: 1.6666667, amp: 0.05, filtMult: 2 ]);
      1.6666667.wait;
    });
  }).play;
  Routine({
    loop({
      if(0.4.coin, {
        durs2.do({
          arg dur;
          Synth(\dusty, [ dur: dur ]);
          dur.wait;
        });
      }, {
        durs3.do({
          arg dur;
          Synth(\dusty, [ dur: dur ]);
          dur.wait;
        });
      });
    });
  }).play;
)

(
  SynthDef(\dva, {
    arg outBus = 0, amp = 0.1, dur = 1, freq = 60;
    var sawEnv = Env.perc(0.03, dur, curve: -2).kr(2);
    var dustEnv = Env.perc(0.05, dur * 0.6, curve: -1).kr(0);
    var saw = LFSaw.ar(
      freq: [
        SinOsc.kr(dur * 8, pi / 4).range(freq * 0.90, freq * 1.10),
        SinOsc.kr(dur * 6, pi / 6).range(freq * 0.96, freq * 1.04)
      ],
      mul: amp
    ) * sawEnv;
    var lpf = RLPF.ar(
      in: saw,
      freq: SinOsc.kr(dur * 1.6, pi / 3).range(freq * 3, freq * 9),
      rq: 0.1
    );
    var dust = Dust2.ar(
      density: [ 128, 64 ],
      mul: amp * 1.2
    ) * dustEnv;
    var mix = Mix([ lpf, dust ]);
    Out.ar(outBus, mix);
  }).add;
)
Synth(\dva, [ freq: 44, dur: 2, amp: 0.1 ])
(
  Routine({
    loop({
      Synth(\dva, [ freq: 44, dur: 1.6666667, amp: 0.1 ]);
      1.6666667.wait;
    });
  }).play;
  Routine({
    loop({
      Synth(\dva, [ freq: 65, dur: 1.6666667, amp: 0.1 ]);
      1.6666667.wait;
    });
  }).play;
)
