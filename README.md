# A Simple MDU

## Default make

Run default `make` to compile chisel and generate binary. 

Run `make verilog` to generate verilog code. The output file is `./vsrc`.

## Simulation

We use `Verilator` for simulation, with the related stimulus tests stored in `./src/cc`.

Run `make sim` to launch simulation by `verilator_sim.mk` in `./scripts`. The default image is dummy.
