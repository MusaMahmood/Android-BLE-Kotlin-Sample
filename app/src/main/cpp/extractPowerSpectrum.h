//
// Academic License - for use in teaching, academic research, and meeting
// course requirements at degree granting institutions only.  Not for
// government, commercial, or other organizational use.
// File: extractPowerSpectrum.h
//
// MATLAB Coder version            : 3.3
// C/C++ source code generated on  : 05-Oct-2017 14:15:24
//
#ifndef EXTRACTPOWERSPECTRUM_H
#define EXTRACTPOWERSPECTRUM_H

// Include Files
#include <cmath>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rtwtypes.h"
#include "extractPowerSpectrum_types.h"

// Function Declarations
extern void extractPowerSpectrum(const double X1_data[], const int X1_size[1],
  const double X2_data[], const int X2_size[1], double Fs, double PSD_data[],
  int PSD_size[2]);
extern void extractPowerSpectrum_initialize();
extern void extractPowerSpectrum_terminate();

#endif

//
// File trailer for extractPowerSpectrum.h
//
// [EOF]
//
