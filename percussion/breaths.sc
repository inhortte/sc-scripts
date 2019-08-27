s.boot;
(
  var hh1Buffer, hh2Buffer, hh3Buffer, snareBuffer, intakeBuffer, h1Freq, h2Freq, h3Freq, hh1Amps, hh2Amps, hh3Amps, hh1Seq, hh2Seq, hh3Seq, tempo, intakeFreq, intakeAmps, intakeSeq;

  tempo = TempoClock(72/60);

  hh1Buffer = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff/hh1.wav");
  hh2Buffer = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff/hh2.wav");
  hh3Buffer = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff/hh3.wav");
  snareBuffer = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff/snare.wav");
  intakeBuffer = Buffer.read(s, "/home/polaris/flavigula/xian/sheriff/intake.wav");

  SynthDef(\samplePlayer, { arg out = 0, bufnum, amp = 0.3, pan = 0;
    Out.ar(out, 
      Pan2.ar(
        PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum)) * amp,
        pan,
        doneAction: Done.freeSelf
      );
    );
  }).add;

  h1Freq = Array.fill(8, 0) ++ [ { rrand(0.1, 0.3) } ];
  h2Freq = Array.fill(12, 0) ++ [ { rrand(0.1, 0.3) } ];
  h3Freq = Array.fill(12, 0) ++ [ { rrand(0.1, 0.3) } ];
  intakeFreq = Array.fill(15, 0) ++ [ 0.4 ];
  intakeFreq.postln;
  hh1Amps = Array.fill(64, { var i; i = 9.rand; h1Freq.at(i) });
  hh2Amps = Array.fill(64, { var i; i = 13.rand; h2Freq.at(i) });
  hh3Amps = Array.fill(64, { var i; i = 13.rand; h3Freq.at(i) });
  intakeAmps = Array.fill(48, { var i; i = 16.rand; intakeFreq.at(i) });
  
  hh1Seq = Pseq(hh1Amps, inf);
  hh2Seq = Pseq(hh2Amps, inf);
  hh3Seq = Pseq(hh3Amps, inf);
  intakeSeq = Pseq(intakeAmps, inf);
  Pbind(
    \instrument, \samplePlayer,
    \bufnum, hh1Buffer,
    \amp, hh1Seq,
    \dur, 0.5,
    \pan, 0
  ).play(tempo);
  Pbind(
    \instrument, \samplePlayer,
    \bufnum, hh2Buffer,
    \amp, hh2Seq,
    \dur, 0.5,
    \pan, -0.8
  ).play(tempo);
  Pbind(
    \instrument, \samplePlayer,
    \bufnum, hh3Buffer,
    \amp, hh3Seq,
    \dur, 0.5,
    \pan, 0.8
  ).play(tempo);
  Pbind(
    \instrument, \samplePlayer,
    \bufnum, snareBuffer,
    \amp, Pseq([ 0, 0, 0, 0.6 ], inf),
    \dur, 1,
    \pan, 0
  ).play(tempo);
  Pbind(
    \instrument, \samplePlayer,
    \bufnum, intakeBuffer,
    \amp, intakeSeq,
    \dur, 1,
    \pan, 0
  ).play(tempo);
)

Rand(0.4, 0.7).value();

(
  var hh1Amps, hh1Stream;
  hh1Amps = Array.fill(16, { var i; i = 5.rand; ~h1Freq.at(i) }).postln;
  hh1Stream = Pseq(hh1Amps, 1).asStream;
  Task({
    var val;
    while {
      val = hh1Stream.next;
      val.notNil;
    } {
      val.postln;
      val.yield;
    }
  }).play
)
