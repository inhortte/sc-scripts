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

    ~chrisBufs = List();
    "/flavigula/xian/ramblings-to-krzys-slices/thurk*.wav".pathMatch.collect { |file|
      ~chrisBufs.add(Buffer.readChannel(s, file, channels: [0]));
    };
    ~silenceBuf = Buffer.readChannel(s, "/flavigula/xian/ramblings-to-krzys-slices/silence.wav", channels: [0]);
    ~backdropBufL = Buffer.readChannel(s, "/flavigula/audacity/algorave-back.wav", channels: [0]);
    ~backdropBufR = Buffer.readChannel(s, "/flavigula/audacity/algorave-back.wav", channels: [1]);
    ~bBakFiles = List();
    ~bBakBufs = List();
    ~aBakFiles = List();
    ~aBakBufs = List();
    ~bFiles = List();
    ~bBufs = List();
    ~aFiles = List();
    ~aBufs = List();
    "/flavigula/ardour/vegkoref/export/b-bak-[12].wav".pathMatch.collect { |file|
      ~bBakFiles.add(PathName(file).fileName);
      ~bBakBufs.add(Buffer.read(s, file));
    };
    "/flavigula/ardour/vegkoref/export/a-bak-[12].wav".pathMatch.collect { |file|
      ~aBakFiles.add(PathName(file).fileName);
      ~aBakBufs.add(Buffer.read(s, file));
    };
    "/flavigula/ardour/vegkoref/export/b-[01][0-9].wav".pathMatch.collect { |file|
      ~bFiles.add(PathName(file).fileName);
      ~bBufs.add(Buffer.read(s, file));
    };
    "/flavigula/ardour/vegkoref/export/a-[0-9].wav".pathMatch.collect { |file|
      ~aFiles.add(PathName(file).fileName);
      ~aBufs.add(Buffer.read(s, file));
    };

    ~melodies1 = List();
    ~melodies2 = List();
    ~melodies1.add([59, 66, 62, 69, 66, 73]); // b fis d a fis cis
    ~melodies1.add([59, 64, 61, 67, 64, 71]); // b e cis g e b
    ~melodies1.add([57, 64, 59, 66, 62, 69]); // a e b fis d a
    ~melodies1.add([55, 64, 66, 61, 57, 61]); // g e fis cis a cis

    g = Group.basicNew(s, 1);
    j = Group.basicNew(s, 1);
    d = Bus.audio(s, 2);
    e = Bus.audio(s, 2);
    SynthDef(\trianglish, {
      var dur = \dur.ir(1.0);
      var freq = \freq.ir(147);
      var env = Env.perc(
        attackTime: 0.1,
        releaseTime: dur * 1.2,
        level: 1,
        curve: -3
      ).kr(2);
      var tri = LFTri.ar(
        freq: freq,
        iphase: [0, 1.3, 2.9, 3.2],
        mul: \amp.ir(0.1) * \mult.ir(1.0)
      ) * env;
      var lpf = RLPF.ar(
        in: tri,
        freq: Line.kr(freq * 4, freq / 4, dur * 0.8),
        rq: 0.2
      );
      Out.ar(
        \outBus.ir(0),
        Splay.ar(lpf)
      );
    }).add;
    SynthDef.new(\hats, {
      arg outBus, atk=0.005, rel=0.3, pan=0, mul=0.2, amp=0.2;
      var sig, range=1, ampEnv, noiseEnv, noiseDurRatio=0.05;

      ampEnv = EnvGen.kr(Env.perc(atk, rel), doneAction:2);
      noiseEnv = EnvGen.kr(Env.perc(atk, rel*noiseDurRatio));

      sig = [345, 432, 543, 654, 765].collect({|freq|
        Pulse.ar(freq, SinOsc.kr(freq / 14));
      });
      sig = HPF.ar(sig, 11000) * 4;
      sig = LPF.ar(
        in: sig,
        freq: LFNoise0.kr(2).range(4500, 9000)
      );
      sig = Mix.new(sig) * ampEnv;
      sig = sig + (PinkNoise.ar * noiseEnv) * amp;

      Out.ar(outBus, Pan2.ar(sig, pan, mul));
    }).add;
    SynthDef(\pleasantPulse, {
      var freq = \freq.ir(220);
      var env = Env.perc(\attack.ir(0.03), \dur.ir(1.0) * \release.ir(0.8), curve: -2).kr(2);
      var pulse = LFPulse.ar(
        freq: [ freq, freq * 0.995, freq * 1.005 ],
        iphase: [ 0.2, 0.6, 0.95 ],
        width: SinOsc.kr(0.2).range(-0.3, 0.3)
      ) * env * \amp.ir(0.1);
      Out.ar(\outBus.ir(0), Splay.ar(pulse));
    }).add;
    SynthDef(\reverb, {
      Out.ar(
        \outBus.ir(0),
        CombN.ar(
          in: In.ar(\inBus.ar(0), 2),
          maxdelaytime: 0.4,
          delaytime: 0.2,
          decaytime: 4
        )
      )
    }).add;
    SynthDef(\reverberation, {
      var sig, wet;
      sig = In.ar(\inBus.ir(0), 2);
      sig = FreeVerb2.ar(sig[0], sig[1], \mix.ir(0.99), \room.ir(0.99), \damp.ir(0.2));
      sig = LPF.ar(sig, \cutoff.ir(1174));  // D6
      Out.ar(\outBus.ir(0), sig);
    }).add;

    SynthDef(\bufplay, {
      arg outBus = 0, bufNum = 0, startPos = 0, dur = 1.0;
      var sig, endPos, lb, idx;
      var env = Env.perc(attackTime: 0.01, releaseTime: dur * 0.9, curve: -2.0);

      startPos = startPos * BufSampleRate.kr(bufNum);

      sig = PlayBuf.ar(
        numChannels: 2, 
        bufnum: bufNum, 
        rate: BufRateScale.kr(bufNum), 
        startPos: startPos, 
        loop: 0,
        doneAction: Done.freeSelf
      );
      Out.ar(outBus, sig );
    }).add;
 
    SynthDef(\grainy, {
      var sig, buf = \buf.ir(~backdropBufL);
      sig = GrainBuf.ar(
        numChannels: 2,
        // trigger: Impulse.kr({ExpRand(60, 60.9)}!12),
        // trigger: Impulse.ar(128),
        trigger: Dust.kr(\dustFreq.ir(128)),
        // dur: Line.kr(1/8, 1/64, BufDur.kr(buf)),
        dur: LFNoise1.kr(\noiseFreq.ir(24)).range(\low.ir(1/128), \high.ir(1/32)),
        // dur: LFNoise1.kr(128).range(0.005, 0.05),
        sndbuf: buf,
        rate: 1.0,
        /*
        pos: (Line.kr(0.0, BufSamples.kr(buf) - 1, BufDur.kr(buf), doneAction: 2) + 
          LFNoise1.kr(128).bipolar(SampleRate.ir * Line.kr(0.04, 0.3, BufDur.kr(buf)))) / 
          BufSamples.kr(buf),
        */
        pos: (Line.kr(0.0, BufSamples.kr(buf) - 1, 1 * BufDur.kr(buf), doneAction: 2) + 
          LFNoise1.kr(128).bipolar(SampleRate.ir * LFNoise1.ar(16).range(0.05, 0.16))) / 
          BufSamples.kr(buf),
        interp: 2,
        pan: LFNoise1.kr(\panFreq.ir(128)).range(-1, 1),
        // pan: 0,
        envbufnum: -1,
        maxGrains: 512,
        mul: \amp.ir(0.4)
      );
      sig = Splay.ar(sig);
      Out.ar(\outBus.ir(0), sig);
    }).add;

    ~bassDurs = [ 
      0.5, 0.5, 1.5, 0.5,
      0.5, 0.5, 0.5, 0.5, 1.5,
      0.5, 0.5, 1.5
    ];
    ~bassBufs = [
      ~bBufs, ~bBufs, ~bBufs, ~bBufs,
      ~bBufs, ~aBufs, ~bBufs, ~aBufs, ~bBufs,
      ~bBufs, ~bBufs, ~bBufs,
      ~bBakBufs, ~aBakBufs
    ];
    ~hatsAmps = Array.fill(128, { [0.2, 0.13, 0.1, 0.25, 0.12, 0.18, 0.12, 0.07, 0.05, 0.03, 0.02, 0.01].choose; }) * 12;
    ~hatsPans = Array.fill(12, { [0.0, -0.60, -0.4, 0.4, 0.6].wchoose([0.6, 0.1, 0.1, 0.1, 0.1]); });

    ~bass = Pbind(
      \instrument, \bufplay,
      \dur, 8, // Pseq(~bassDurs, inf),
      \bufNum, Pseq(~bassBufs, inf).collect({ |bufs| bufs.choose })
    );
    ~chris = Pbind(
      \instrument, \grainy,
      \group, g,
      \addAction, 0,
      \dustFreq, Pwhite(24, 98, inf),
      \noiseFreq, Pwhite(32, 96, inf),
      \amp, 0.9,
      \buf, Pseq(Array.fill(119, { [~chrisBufs.choose, ~silenceBuf].wchoose([0.1, 0.9]) }), 1)
    );
    ~triDurs = Array.fill(64, { [2, 4, 8, 0.5, 1, 1.333333333, 0.66666666666].wchoose([0.4, 0.2, 0.1, 0.1, 0.05, 0.05, 0.05]) });
    ~tri = Pbind(
      \instrument, \trianglish,
      \group, g,
      \addAction, 0,
      \outBus, d,
      \amp, Prand([0.4, 0.5, 0.6, 0.7, 0.8], inf),
      \mult, 0.7,
      \freq, Pseq(Array.fill(127, { ~melodies1.choose.midicps; }).flatten, Prand([1, 2, 3], 1)),
      \dur, Pseq(~triDurs, inf)
    );
    ~hats = Pbind(
      \instrument, \hats,
      \group, g,
      \addAction, 0,
      \outBus, 0,
      \dur, Prand([2.02, 2.0, 1.98, 2.06, 1.94], inf),
      \amp, Pseq(~hatsAmps, inf),
      \pan, Pseq(~hatsPans, inf)
    );
    ~hats2 = Pbind(
      \instrument, \hats,
      \group, g,
      \addAction, 0,
      \outBus, 0,
      \dur, Pwrand([0.25, 1.0, 0.5], [0.2, 0.5, 0.3], inf),
      \amp, Prand([0.9, 0.7, 0.8], inf),
      \pan, Pseq(~hatsPans, inf)
    );
    ~pulse = Pbind(
      \instrument, \pleasantPulse,
      \group, g,
      \addAction, 0,
      \outBus, d,
      \amp, 0.3,
      \freq, Pseq([49, 49, 90].midicps, inf),
      \dur, Pseq([0.25, 2.75, 13], inf),
      \attack, Pseq([0.001, 0.03, 0.5], inf),
      \release, Pseq([0.4, 0.5, 0.05], inf)
    );
  });
)

Synth.tail(g, \reverb, [ outBus: 0, inBus: d ]);
Synth.tail(g, \reverberation, [ outBus: 0, inBus: d ]);
m = Synth.head(g, \grainy, [\amp, 0.9])
n = Synth.head(g, \grainy, [\buf, ~backdropBufR, \dustFreq, 256, \noiseFreq, 48, \amp, 0.8]);
~chris.play(TempoClock(12/60));
(
  t = ~tri.play;
  t.mute;
  h = ~hats.play;
  h.mute;
  i = ~hats2.play;
  i.mute;
  k = ~bass.play;
  k.mute;
  p = ~pulse.play;
  p.mute;
)
t.mute
t.unmute;
h.mute;
h.unmute;
i.mute;
i.unmute;
k.mute;
k.unmute;
p.mute;
p.unmute;
~bastard = Synth(\bufplay, [ bufNum: ~bBufs.at(0) ]);
~bBufs.choose
(
  ~bassDurs = [ 
    0.5, 0.5, 1.5, 0.5,
    0.5, 0.5, 0.5, 0.5, 1.5,
    0.5, 0.5, 1.5
  ];
  [0, 1, 2].collect({ ~bassDurs.choose });
)
