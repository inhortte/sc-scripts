(
  s.options.numBuffers = 8192; 
  s.boot;
)

// load soundfiles to buffers
(
	// LOAD SOUND FILES

	// prep collections (lists/arrays) holding names and references to buffers
	~granBfrList = List();
	~granBfr = List();

	// scrape a folder and load all files from it
    // YOU NEED A FOLDER "smp" next to this scd file with JUST wav files 
	postln(" .  Loading samples for granular synthesis ..." );
	"/home/polaris/flavigula/xian/voice-messages/*.wav".pathMatch.collect { |file|
		
		(">>> loading" + PathName(file).fileName).postln;
		~granBfrList.add(PathName(file).fileName);
    ~granBfr.add(Buffer.read(s, file));
		// ~granBfr.add(Buffer.readChannel(s, file, channels: [0]));
	};

	" ".postln;

  // Synth definition - simple buffer player
  SynthDef(\bufplayer, { | out = 0, bufnum = 0, startpos = 0, amount = 16, gate = 1 |
    var signal, endpos, lb, idx;
    // var env = Linen.kr(gate: gate, releaseTime: 0.01, doneAction: Done.freeSelf);
    var env = Env.perc(attackTime: 0.01, releaseTime: 0.5, curve: 0.9, doneAction: Done.freeSelf);

    startpos = startpos * BufSampleRate.kr(bufnum);
    endpos = startpos + (amount * BufSampleRate.kr(bufnum));
    lb = LocalBuf.new(amount * BufSampleRate.kr(bufnum), 1);
    idx = 0;
    bufnum.getn(startpos, endpos, { |d|
      lb.set(idx, d);
      idx = idx + 1;
    });

    signal = PlayBuf.ar(numChannels: 1, bufnum: bufnum, rate: BufRateScale.kr(bufnum), startPos: startpos, loop: 1.0);
    signal = signal * env;
    Out.ar(out, signal!2 );
  }).add;
)

~playbuf1 = Synth(\bufplayer, [ bufnum: ~granBfr.at(0), startpos: 0 ]);
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
( 
// PATTERN

Pbind(*[
	instrument: \bufplayer,
	
	bufnum:
	Pseq([0,1,2,3,4,5,7,8,Rest()], inf),

	dur:
	Pseq(Array.fill(9, { 0.1.linrand }).put(8,7), inf),
	
	startpos:
	Pseq(
		Array.fill(4, { 180.rand })
		.addAll(Prand(Array.fill(10, { 180.rand }),1) ! 2)
		.addAll(Array.fill(3, { 180.rand })), inf),
	
    legato:
	Prand([0.95,0.98,0.9,1,0.3], inf),
	
]).play;

)

