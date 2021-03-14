(
  s.options.numBuffers = 8192; 
  s.boot;
)
s.plotTree;

(
  ~fileList = List();
  ~bufferList = List();

  "/home/polaris/flavigula/xian/voice-messages/*.wav".pathMatch.collect { |file|

    ~fileList.add(PathName(file).fileName);
    ~bufferList.add(Buffer.read(s, file));
    // ~bufferList.add(Buffer.readChannel(s, file, channels: [0]));
  };

  SynthDef(\bufplayer, {
    arg out = 0, bufnum = 0, startpos = 0, amount = 16, gate = 1;
    var signal, endpos, lb, idx;
    var env = Env.perc(attackTime: 0.01, releaseTime: 0.5, curve: 0.9, doneAction: Done.freeSelf);

    startpos = startpos * BufSampleRate.kr(bufnum);
    endpos = startpos + (amount * BufSampleRate.kr(bufnum));
    lb = LocalBuf.new(amount * BufSampleRate.kr(bufnum), 1);
    idx = 0;
    bufnum.getn(startpos, endpos, { |d|
      lb.set(idx, d);
      idx = idx + 1;
    });

    signal = PlayBuf.ar(
      numChannels: 1, 
      bufnum: bufnum, 
      rate: BufRateScale.kr(bufnum), 
      startPos: startpos, 
      loop: 1.0
    ) * env;
    Out.ar(out, signal ! 2 );
  }).add;
)

~playbuf1 = Synth(\bufplayer, [ bufnum: ~bufferList.at(0), startpos: 0 ]);
// test if your synth works:
//~playbuf1 = Synth(\bufplayer, [ bufnum: 0, startpos: 4;]);

(
  s.freeAllBuffers;
)

(
  {
    var clock, pan;
    ~yapping = Buffer.read(s, "/home/polaris/flavigula/xian/voice-messages/yapping.wav");
    clock = Impulse.kr(1.2);
    pan = WhiteNoise.kr(0.6);
    TGrains.ar(numChannels: 2, trigger: clock, bufnum: ~yapping, centerpos: 2000000, dur: 0.5, pan: pan, amp: 0.5);
  }.play
)

(
  var clock;
  ~gransSixteenths = List();
  ~gransEighths = List();
  ~gransTrips = List();
  "/home/polaris/flavigula/xian/voice-messages/granules-sixteenth/*".pathMatch.collect { |file|
    ~gransSixteenths.add(Buffer.read(s, file));
  };
  "/home/polaris/flavigula/xian/voice-messages/granules-eighths/*".pathMatch.collect { |file|
    ~gransEighths.add(Buffer.read(s, file));
  };
  "/home/polaris/flavigula/xian/voice-messages/granules-trips/*".pathMatch.collect { |file|
    ~gransTrips.add(Buffer.read(s, file));
  };
)

(
  SynthDef(\granPlayer, { | out = 0, buf = 0, gate = 1, level = 1.0, dur = 1 |
    var signal, filter;
    var env = Linen.kr(gate: gate, releaseTime: 0.1, susLevel: level, doneAction: Done.freeSelf);
    signal = PlayBuf.ar(numChannels: 1, bufnum: buf, rate: BufRateScale.kr(buf), doneAction: Done.freeSelf);
    signal = signal * env;
    filter = LPF.ar(in: signal, freq: XLine.kr(start: 4000, end: 24, dur: dur, doneAction: Done.freeSelf));
    Out.ar(filter, signal ! 2);
  }).add;
)

// utils
(
  ~randSamples = { |n = 16, bufList|
    Array.linrand(n, 0, bufList.size - 1).collect { |idx| bufList.at(idx) };
  }
)
Array.linrand(16, 0, ~gransSixteenths.size - 1).collect { |idx| ~gransSixteenths.at(idx) };
~randSamples.value(16, ~gransSixteenths);

// Sixteenths
(
  var clock = TempoClock(72/60);
  var seq = Prand(~gransSixteenths, 176).asStream;
  Routine({
    var buf;
    while {
`     buf = seq.next;
      buf.notNil;
    } {
      Synth(\granPlayer, [\buf, buf, \level, [0.2, {Rand(0.4, 0.6)}], \dur, 0.1]);
      0.2083333.wait;
    }
  }).play;
)

// Eighths
(
  var clock = TempoClock(72/60);
  var seq = Prand(~gransEighths, 64).asStream;
  Routine({
    var buf;
    while {
`     buf = seq.next;
      buf.notNil;
    } {
      Synth(\granPlayer, [\buf, buf, \level, 0.4, \dur, 0.14]);
      0.4166666666666667.wait;
    }
  }).play;
)

// Triplets
(
  var clock = TempoClock(72/60);
  var seq = Prand(~gransTrips, 48).asStream;
  Routine({
    var buf;
    while {
`     buf = seq.next;
      buf.notNil;
    } {
      Synth(\granPlayer, [\buf, buf, \level, [0.2, {Rand(0.4, 0.6)}], \dur, 0.167]);
      0.2777777777777778.wait;
    }
  }).play;
)
 
