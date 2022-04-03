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
   // MIDIClient.destinations;
    ~rasterOut = MIDIOut(0);    
    ~rasterOut.connect(6);
    ~burp = Routine({
      loop({
        if(
          0.125.coin,
          {
            ~rasterOut.control(0, ctlNum: 90, val: 127);
            (60.0/72.0/2).wait;
            ~rasterOut.control(0, ctlNum: 90, val: 0);
          },
          {
            0.125.coin.postln;
            (60.0/72.0/2).wait;
          }
        );
      });
    });
  });
)
a = ~burp.play;
a.stop;
MIDIClient.destinations.at(6).postln; // Raster (for now!)
(
  ~rasterOut.control(0, ctlNum: 90, val: 127);
)
(
  ~rasterOut.control(0, ctlNum: 90, val: 0);
)
(
  ~rasterOut.control(0, ctlNum: 80, val: 127);
  // ~rasterOut.control(0, ctlNum: 80, val: 0);
  ~rasterOut.control(0, ctlNum: 80, val: 0);
)
