//
// Academic License - for use in teaching, academic research, and meeting
// course requirements at degree granting institutions only.  Not for
// government, commercial, or other organizational use.
// File: ssvep_filter_f32.cpp
//
// MATLAB Coder version            : 3.3
// C/C++ source code generated on  : 03-Nov-2017 18:02:47
//

// Include Files
#include "ssvep_filter_f32.h"

// Function Declarations
static void filter(double b[7], double a[7], const double x[1036], const double
                   zi[6], double y[1036]);
static void flipud(double x[1036]);

// Function Definitions

//
// Arguments    : double b[7]
//                double a[7]
//                const double x[1036]
//                const double zi[6]
//                double y[1036]
// Return Type  : void
//
static void filter(double b[7], double a[7], const double x[1036], const double
                   zi[6], double y[1036])
{
  double a1;
  int k;
  int naxpy;
  int j;
  a1 = a[0];
  if ((!(a[0] == 0.0)) && (a[0] != 1.0)) {
    for (k = 0; k < 7; k++) {
      b[k] /= a1;
    }

    for (k = 0; k < 6; k++) {
      a[k + 1] /= a1;
    }

    a[0] = 1.0;
  }

  for (k = 0; k < 6; k++) {
    y[k] = zi[k];
  }

  memset(&y[6], 0, 1030U * sizeof(double));
  for (k = 0; k < 1036; k++) {
    naxpy = 1036 - k;
    if (!(naxpy < 7)) {
      naxpy = 7;
    }

    for (j = 0; j + 1 <= naxpy; j++) {
      y[k + j] += x[k] * b[j];
    }

    naxpy = 1035 - k;
    if (!(naxpy < 6)) {
      naxpy = 6;
    }

    a1 = -y[k];
    for (j = 1; j <= naxpy; j++) {
      y[k + j] += a1 * a[j];
    }
  }
}

//
// Arguments    : double x[1036]
// Return Type  : void
//
static void flipud(double x[1036])
{
  int i;
  double xtmp;
  for (i = 0; i < 518; i++) {
    xtmp = x[i];
    x[i] = x[1035 - i];
    x[1035 - i] = xtmp;
  }
}

//
// [5 40] bandpass butterworth N=3; 250Hz
//  X = X(:);
// Arguments    : const double X[1000]
//                float Y[1000]
// Return Type  : void
//
void ssvep_filter_f32(const double X[1000], float Y[1000])
{
  double d0;
  double d1;
  int i;
  double y[1036];
  double dv0[7];
  static const double dv1[7] = { 0.0418768282347742, 0.0, -0.125630484704323,
    0.0, 0.125630484704323, 0.0, -0.0418768282347742 };

  double dv2[7];
  static const double dv3[7] = { 1.0, -3.99412602172993, 6.79713743558926,
    -6.44840721730666, 3.65712515526032, -1.17053739881085, 0.159769122451512 };

  double b_y[1036];
  double a[6];
  static const double b_a[6] = { -0.041876828234757295, -0.041876828234824783,
    0.083753656469613066, 0.0837536564695041, -0.041876828234757114,
    -0.04187682823477689 };

  //  Optimized SSVEP C Filter:
  d0 = 2.0 * X[0];
  d1 = 2.0 * X[999];
  for (i = 0; i < 18; i++) {
    y[i] = d0 - X[18 - i];
  }

  memcpy(&y[18], &X[0], 1000U * sizeof(double));
  for (i = 0; i < 18; i++) {
    y[i + 1018] = d1 - X[998 - i];
  }

  for (i = 0; i < 7; i++) {
    dv0[i] = dv1[i];
    dv2[i] = dv3[i];
  }

  for (i = 0; i < 6; i++) {
    a[i] = b_a[i] * y[0];
  }

  memcpy(&b_y[0], &y[0], 1036U * sizeof(double));
  filter(dv0, dv2, b_y, a, y);
  flipud(y);
  for (i = 0; i < 7; i++) {
    dv0[i] = dv1[i];
    dv2[i] = dv3[i];
  }

  for (i = 0; i < 6; i++) {
    a[i] = b_a[i] * y[0];
  }

  memcpy(&b_y[0], &y[0], 1036U * sizeof(double));
  filter(dv0, dv2, b_y, a, y);
  flipud(y);
  for (i = 0; i < 1000; i++) {
    Y[i] = (float)y[i + 18];
  }
}

//
// Arguments    : void
// Return Type  : void
//
void ssvep_filter_f32_initialize()
{
}

//
// Arguments    : void
// Return Type  : void
//
void ssvep_filter_f32_terminate()
{
  // (no terminate code required)
}

//
// File trailer for ssvep_filter_f32.cpp
//
// [EOF]
//
