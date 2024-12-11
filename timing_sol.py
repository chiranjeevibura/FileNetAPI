#!/usr/bin/env python3

import numpy as np
import sys
import time

import numpy as np
import sys
import time

def main():
    # Parse command-line arguments
    if len(sys.argv) != 5:
        print("Usage: python timing_sol.py -nt M -nx N")
        sys.exit(1)

    niter = 0
    nx = 0

    for i in range(1, len(sys.argv), 2):
        if sys.argv[i] == "-nt":
            niter = int(sys.argv[i + 1])
        elif sys.argv[i] == "-nx":
            nx = int(sys.argv[i + 1])

    a = np.full(nx, 1.0, dtype=np.float64)  
    b = np.full(nx, 2.0, dtype=np.float64)  
    c = np.full(nx, 5.0, dtype=np.float64)  
    r = np.zeros(nx, dtype=np.float64)      

    loop_start = time.time()

    for n in range(niter):
        r[:] = a[:] * b[:] + c[:]  

    loop_end = time.time()
    walltime = loop_end - loop_start

    print('Number of points: ', nx)
    print('Number of iterations: ', niter)
    print('Elapsed time: ', walltime)
    print('Elapsed time per iteration: ', walltime / float(niter))
    print('Elapsed time per iteration per point: ', walltime / float(niter) / float(nx))

if __name__ == "__main__":
    main()
