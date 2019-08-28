s.boot;
// load soundfiles to buffers
(
	// LOAD SOUND FILES

	// prep collections (lists/arrays) holding names and references to buffers
	~granBfrList = List();
	~granBfr = List();

	// scrape a folder and load all files from it
    // YOU NEED A FOLDER "smp" next to this scd file with JUST wav files 
	postln(" .  Loading samples for granular synthesis ..." );
	"smp/*".resolveRelative.pathMatch.collect { |file|
		
		(">>> loading" + PathName(file).fileName).postln;
		~granBfrList.add(PathName(file).fileName);
		~granBfr.add(Buffer.readChannel(s, file, channels: [0]));
	};

	" ".postln;


// Synth definition - simple buffer player
SynthDef(\bufplayer, {
	arg out = 0, bufnum =0, startpos = 0, gate = 1;
	var signal;
	var env = Linen.kr(gate, releaseTime: 0.01, doneAction: Done.freeSelf);

	startpos = startpos * BufSampleRate.kr(bufnum);

	signal = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), startPos: startpos, loop: 1.0);
	signal = signal * env;
	Out.ar(out, signal!2 );
}).add;

)

// test if your synth works:
//~playbuf1 = Synth(\bufplayer, [ bufnum: 0, startpos: 4;]);


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

