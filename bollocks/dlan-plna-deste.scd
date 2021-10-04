s.plotTree;
(
  s.options.numBuffers = 8192; 
  s.newBusAllocators;
  s.waitForBoot({
    s.freeAll;
    Buffer.freeAll;
    s.sync;

    MIDIClient.disposeClient;
    MIDIClient.init; 
    ~midiOut = MIDIOut(0);
    // MIDIClient.destinations;
    ~midiOut.connect(2);
    ~midiOut.control(0, ctlNum: 20, val: 108);
    ~introCutoff = Routine({
      loop({
        108.do({ |i|
          var cutoff = ((i / 108) * 80).asInteger + 48;
          ~midiOut.control(0, ctlNum: 43, val: cutoff);
          (1/8).wait;
        });
        108.do({ |i|
          var cutoff = 127 - (((i / 108) * 80).asInteger);
          ~midiOut.control(0, ctlNum: 43, val: cutoff);
          (1/8).wait;
        });
      });
    });
    ~introTrem = Routine({
      loop({
        60.do({ |i| 
          var base = i + 48;
          var high = base + 10;
          var low = base - 10;
          var firstWait = { rrand(0.0625, 0.5) }.value;
          firstWait.postln;
          ~midiOut.control(0, ctlNum: 54, val: base);
          firstWait.wait;
          ~midiOut.control(0, ctlNum: 54, val: high);
          ({ rrand(0.0625, 0.5) }.value).wait;
          ~midiOut.control(0, ctlNum: 54, val: low);
          ({ rrand(0.0625, 0.5) }.value).wait;
          ~midiOut.control(0, ctlNum: 54, val: base);
          ({ rrand(0.0625, 0.5) }.value).wait;
          (1/12).wait;
        });
        60.do({ |i| 
          var base = 108 - i;
          var high = base + 10;
          var low = base - 10;
          var firstWait = { rrand(0.0625, 0.5) }.value;
          firstWait.postln;
          ~midiOut.control(0, ctlNum: 54, val: base);
          firstWait.wait;
          ~midiOut.control(0, ctlNum: 54, val: high);
          ({ rrand(0.0625, 0.5) }.value).wait;
          ~midiOut.control(0, ctlNum: 54, val: low);
          ({ rrand(0.0625, 0.5) }.value).wait;
          ~midiOut.control(0, ctlNum: 54, val: base);
          ({ rrand(0.0625, 0.5) }.value).wait;
          (1/12).wait;
        });
      });
    });

    SynthDef(\sawFinale, {
      var sum, temp, lpf;
      var freq = \freq.ir(220);
      var env = EnvGen.ar(
        Env.adsr(
          attackTime: \atk.ir(1),
          decayTime: 0,
          sustainLevel: 1,
          releaseTime: \rel.ir(4),
          curve: -2
        ),
        gate: \gate.ir(1),
        levelScale: \amp.ir(0.1),
        doneAction: 2
      );
      sum = 0;
      6.do{
        temp = VarSaw.ar(
          freq: [freq, freq * 1.01, freq * 0.99],
          iphase: {Rand(0.0, 1.0)}!2,
          width: {Rand(0.005, 0.1)}!2
        );
        sum = sum + temp;
      };
      sum = sum * 0.1 * env;
      lpf = RLPF.ar(
        in: sum,
        freq: [Line.kr(freq * 6.0, freq * 3.0, \dur.ir(20)), Line.kr(freq * 7.0, freq * 2.0, \dur.ir(20))],
        rq: [{Rand(0.2, 0.4)}, {Rand(0.2, 0.5)}]
      );
      Out.ar(\outBus.ir(0), lpf);
    }).add;

    ~mullahFinaleMidi = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \dur, Pwrand([Pwhite(0.5, 4.0, 5), Pwhite(0.25, 1.5, 2), Pwhite(0.125, 1.0, 3)], [0.7, 0.2, 0.1], inf),
      \midinote, Pwrand([Pseq([57, 58, 60, 65, 64], 1), Prand([60, 58, 57], 2), Pseq([60, 65, 57], 2), Prand([64, 65], 3)], 
        [0.4, 0.3, 0.2, 0.1],
        inf),
      \legato, Pwhite(0.6, 0.9, inf),
      \hasGate, true
    );

    ~improv20210924 = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \dur, 0.5,
      // \midinote, Pwrand([Pn(Pseq([35, 42, 49], {rrand(1, 2)}), 3)], [1.0], inf),
      \midinote, Pwrand([Pn(Pseq([35, 42, 49], {rrand(1, 2)}), 3), Pn(Pseq([37, 44, 54], {rrand(2, 4)}), 2), Pn(Prand([35, 42, 49, 37, 44, 54], 6), 2)], [0.7, 0.2, 0.1], inf),
      \legato, Pwhite(0.2, 0.4, inf),
      \hasGate, true
    );
  });
)

i = ~introCutoff.play;
i.stop;
j = ~introTrem.play;
j.stop;
k = ~mullahFinaleMidi.play;
k.stop;
~midiOut.allNotesOff(0);

m = Synth(\sawFinale, [\atk, 4, \rel, 6, \amp, 0.1, \freq: 81.midicps]);
m.set(\gate, 0);
m.free;

n = ~improv20210924.play;
n.stop;

{ rrand(0.0625, 0.5) }.value;

