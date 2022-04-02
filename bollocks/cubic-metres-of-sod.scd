s.plotTree
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
    ~midiOut.connect(4);
    MIDIClient.destinations;
    s.sync;
    // ~midiOut.control(0, ctlNum: 117, val: 127);

    ~mullahIntroSweepDepth = Routine({
      loop({
        64.do({ |i|
          var sweepDepth = ((i / 64) * 32).asInteger + 64;
          ~midiOut.control(0, ctlNum: 45, val: sweepDepth);
          (81/60/9).wait;
        });
        64.do({ |i|
          var sweepDepth = 127 - (((i / 64) * 32).asInteger);
          ~midiOut.control(0, ctlNum: 45, val: sweepDepth);
          (81/60/9).wait;
        });
      });
    });
    ~mullahIntroSweepRate = Routine({
      loop({
        64.do({ |i|
          var sweepRate = ((i / 64) * 48).asInteger + 64;
          ~midiOut.control(0, ctlNum: 46, val: sweepRate);
          (81/60/5).wait;
        });
        64.do({ |i|
          var sweepRate = 127 - (((i / 64) * 48).asInteger);
          ~midiOut.control(0, ctlNum: 46, val: sweepRate);
          (81/60/5).wait;
        });
      });
    });
    ~mullahIntroModulation = Routine({
      loop({
        var cutoff = rrand(16, 48).asInteger;
        var shape = rrand(80, 108).asInteger;
        ~midiOut.control(0, ctlNum: 43, val: cutoff);
        ~midiOut.control(0, ctlNum: 54, val: shape);
        (81/60/4).wait;
      });
    });
    ~mullahDTModulation = Routine({
      loop({
        var cutoff = rrand(24, 32).asInteger;
        var shape = rrand(0, 24).asInteger;
        ~midiOut.control(0, ctlNum: 43, val: cutoff);
        ~midiOut.control(0, ctlNum: 54, val: shape);
        (81/60/4).wait;
      });
    });
    ~melodyEphr = Array.fill(8, [ 61, 57, 59, 52, 55 ]).flatten; // cis a b e g (Edor - playing G lyd pent)
    ~melodyEb9 = Array.fill(8, [ 63, 58, 61, 53, 55 ]).flatten; // ees bes des f g (Eb9 - playing Db lyd pent)
    ~melodyEm6 = Array.fill(8, [ 64, 59, 62, 54, 57 ]).flatten; // e b d fis a (Em6 - playing D pent)
    ~melodyAqu = Array.fill(8, [ 65, 60, 62, 55, 57 ]).flatten; // f c d g a (Aqu - playing F pent)
    ~melodyCisqu = Array.fill(8, [ 66, 61, 63, 57, 59 ]).flatten; // fis cis dis a b (Cisqu - playing A lyd pent)
    ~melodiesFlat = [
      ~melodyEphr, ~melodyEb9, ~melodyEm6, ~melodyAqu, ~melodyCisqu,
      ~melodyEphr, ~melodyEb9, ~melodyEm6
    ].flatten;
    ~durs = Array.fill(320, {
      [0.25, [0.125, 0.125]].wchoose([0.9, 0.1]);
    });
    ~melodies = [];
    0.for(~durs.size, { |i|
      if(~durs.at(i).class == Array,
        {
          ~melodies = ~melodies ++ [~melodiesFlat.at(i), ~melodiesFlat.at(i)];
        },
        {
          ~melodies = ~melodies ++ [~melodiesFlat.at(i)];
        }
      )
    });
    ~melodiesPlano = [ ~melodyAqu, ~melodyCisqu, ~melodyEphr, ~melodyEb9, ~melodyEm6, ~melodyAqu ].flatten;
    ~dursDT = Array.fill(240, { [[0.125, 0.125], [0.0625,0.0625,0.0625,0.0625]].wchoose([0.95, 0.05]); });
    ~melodiesDT = [];
    ~dursDT.size.postln;
    0.for(~dursDT.size - 1, { |i|
      var duration = ~dursDT.at(i);
      if(duration != nil, 
        ~melodiesDT = ~melodiesDT ++ Array.fill(duration.size, ~melodiesPlano.at(i));
      );
    });
    ~melodiesDT.size.postln;
    "bastard".postln;
    ~amps = {
      var amps = [];
      var idx = 0;
      var sound = false;
      while({idx < ~melodiesDT.size}, {
        var chunk;
        if(sound, {
          chunk = Array.fill(rrand(16, 48), { [0.0, 0.2, 0.4, 0.6, 0.8, 1.0].choose });
        }, {
          chunk = Array.fill(rrand(1, 32), 0.0);
        });
        amps = amps ++ chunk;
        idx = idx + chunk.size;
        sound = sound.not;
      });
      amps;
    }.value;
    ~mullahIntro = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \midinote, Pseq(~melodies, 1),
      \legato, Pwhite(0.6, 0.9, inf),
      \dur, Pseq(~durs.flatten, 1),
      \hasGate, true
    );
    ~mullahIntroDT = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \midinote, Pseq(~melodiesDT, 1),
      \dur, Pseq(~dursDT.flatten, 1),
      \amp, Pseq(~amps, 1),
      \hasGate, true
    );
    ~mullahFoghorn = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \midinote, Pseq([ 36, 34, 32 ], 1), // c bes
      \dur, Pseq([10, 10, 12], 1),
      \hasGate, true
    );
    ~mullahFoghorn2 = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \midinote, Pseq([ 44, 44, 42, 42, 41, 41 ], 1), 
      \dur, 5,
      \hasGate, true
    );
    ~mullahBellTower = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \midinote, Pseq([ 35, 34, 35, 36, 33, 35, 34, 35 ], 1), // b bes b c aes b bes b
      \dur, 10,
      \hasGate, true
    );
    ~rodential = Pbind(
      \type, \midi,
      \midicmd, \noteOn,
      \midiout, ~midiOut,
      \chan, 0,
      \midinote, Prand([33, 35, 40], inf),
      \dur, Pwrand([Pwhite(0.5, 5.0, rrand(1, 4)), Pseq([0.25, 0.125, 0.5, 0.25, 0.125], rrand(1, 3))], [0.9, 0.1], inf),
      \hasGate, true
    );
  });
)
~rodential.play(TempoClock(81/60));jj

i = ~mullahIntroSweepDepth.play;
i.stop;
k = ~mullahIntroSweepRate.play;
k.stop;
l = ~mullahIntroModulation.play;
l.stop;
x = ~mullahDTModulation.play;
x.stop;
j = ~mullahIntro.play(TempoClock(81/60));
j.stop;
y = ~mullahIntroDT.play(TempoClock(81/60));
y.stop;
n = ~mullahFoghorn.play(TempoClock(81/60));
n.stop;
n = ~mullahFoghorn2.play(TempoClock(54/60));
m = ~mullahBellTower.play(TempoClock(81/60));
m.stop;
~midiOut.control(0, ctlNum: 43, val: 40);
Array.fill(8, ~melodyEphr).flatten.size;
Array.fill(2, 0.125).at(0);
nil != nil
(
  var bastard = [];
  var idx = 0;
  var sound = false;
  while({idx < 26},
  {
    var chunk;
    if(sound, {
      chunk = Array.fill(rrand(1, 5), { [0.0, 0.2, 0.4, 0.6, 0.8, 1.0].choose });
    }, {
      chunk = Array.fill(rrand(1, 5), 0.0);
    });
    bastard = bastard ++ chunk;
    idx = idx + chunk.size;
    sound = sound.not;
  });
  bastard;
)
