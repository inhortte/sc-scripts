(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  MIDIClient.init;
  MIDIClient.destinations;
)
MIDIClient.destinations;
Array.rand(15, 5, 14) ++ Array.rand(4, 17, 20);
[1, 3, 5, 7, 9].sputter(0.33, 20);
Array.fill(128, { [0.5, 1, 1.5, 1.3333333, 0.666666].wchoose([0.8, 0.05, 0.05, 0.05, 0.05]) });
(
  var clock = TempoClock(90/60);
  var aMajor = [ 9, 11, 13, 14, 16, 18, 20 ];
  var bbMajor = [ 10, 12, 14, 15, 17, 19, 21 ];
  var nts = MIDIClient.destinations[3].uid;
  var midiPattern = MIDIOut(0, nts);
  var midiCutoff = MIDIOut(0, nts);
  var dorian7 = [1, 3, 5, 7, 9];
  var bulk, cutoff, resonance, pwm;
  ~bulbous = Prand(dorian7.sputter(0.333, 100), inf);
  ~durations = [0.5, 1, 1.5, 1.3333333, 0.666666];
  bulk = Pbind(
    \scale, bbMajor,
    \degree, ~bulbous,
    \dur, Prand(Array.fill(128, { ~durations.wchoose([0.8, 0.05, 0.05, 0.05, 0.05]) }), inf)
    // \dur, Prand(Array.fill(128, { [0.25, 0.125, 0.0625, 0.5].wchoose([0.9, 0.1, 0.0, 0.0]) }), inf)
  );
  cutoff = Pbind(
    \control, Pbrown(20, 72, step: 3, length: inf)
  );
  resonance = Pbind(
    \control, Prand([42, 84, 56, 96], inf), 
    \dur, Prand([0.125, 0.0625, 0.25], inf)
  );
  pwm = Pbind(
    \control, Pbrown(36, 72, step: 4, length: inf)
  );
  ~patters = (bulk <> (type: \midi, midicmd: \noteOn, midiout: midiPattern, sustain: 0.5)).play(clock);
  (cutoff <> (type: \midi, midicmd: \control, midiout: midiPattern, ctlNum: 43)).play(clock);
  (resonance <> (type: \midi, midicmd: \control, midiout: midiPattern, ctlNum: 44)).play(clock);
  (pwm <> (type: \midi, midicmd: \control, midiout: midiPattern, ctlNum: 54)).play(clock);
)
(
  ~aMajor = [ 9, 11, 13, 14, 16, 18, 20 ];
  ~bbMajor = [ 10, 12, 14, 15, 17, 19, 21 ];
)

(
  ~patters.stream = Pbind(
    \scale, ~bbMajor,
    \degree, ~bulbous,
    \dur, Prand(Array.fill(128, { ~durations.wchoose([0.8, 0.05, 0.05, 0.05, 0.05]) }), inf)
  );
)

(
  ~patters.stream = Pbind(
    \scale, ~aMajor,
    \degree, ~bulbous,
    \dur, Prand(Array.fill(128, { ~durations.wchoose([0.8, 0.05, 0.05, 0.05, 0.05]) }), inf)
  );
)
~patters.stop
(
  var nts = MIDIClient.destinations[3].uid;
  var midi = MIDIOut(0, nts);
  midi.allNotesOff(0);
)
