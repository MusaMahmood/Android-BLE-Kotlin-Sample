//
// Academic License - for use in teaching, academic research, and meeting
// course requirements at degree granting institutions only.  Not for
// government, commercial, or other organizational use.
// File: extractPowerSpectrum.cpp
//
// MATLAB Coder version            : 3.3
// C/C++ source code generated on  : 05-Oct-2017 14:15:24
//

// Include Files
#include "rt_nonfinite.h"
#include "extractPowerSpectrum.h"
#include "extractPowerSpectrum_emxutil.h"

// Function Declarations
static void b_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1]);
static void b_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out);
static void b_r2br_r2dit_trig(const emxArray_creal_T *x, int n1_unsigned, const
  emxArray_real_T *costab, const emxArray_real_T *sintab, emxArray_creal_T *y);
static void b_repmat(double a, double varargin_1, double b_data[], int b_size[1]);
static void bluestein_setup(int nRows, emxArray_creal_T *wwc);
static void c_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1]);
static void c_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out);
static void c_r2br_r2dit_trig(const emxArray_creal_T *x, int n1_unsigned, const
  emxArray_real_T *costab, const emxArray_real_T *sintab, emxArray_creal_T *y);
static void d_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1]);
static void d_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out);
static void dobluesteinfft(const double x_data[], const int x_size[1], int N2,
  int n1, const emxArray_real_T *costab, const emxArray_real_T *sintab, const
  emxArray_real_T *sintabinv, emxArray_creal_T *y);
static void e_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1]);
static void e_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out);
static void fft(const double x_data[], const int x_size[1], emxArray_creal_T *y);
static void filter(const double x_data[], const int x_size[1], const double zi[6],
                   double y_data[], int y_size[1]);
static void filtfilt(const double x_in_data[], const int x_in_size[1],
                     emxArray_real_T *y_out);
static void flipud(double x_data[], int x_size[1]);
static void generate_twiddle_tables(int nRows, boolean_T useRadix2,
  emxArray_real_T *costab, emxArray_real_T *sintab, emxArray_real_T *sintabinv);
static void get_algo_sizes(int n1, boolean_T useRadix2, int *N2blue, int *nRows);
static void hannWin(double x, double w_data[], int w_size[1]);
static double mean(const emxArray_real_T *x);
static void power(const double a_data[], const int a_size[1], double y_data[],
                  int y_size[1]);
static void r2br_r2dit_trig(const double x_data[], const int x_size[1], int
  n1_unsigned, const emxArray_real_T *costab, const emxArray_real_T *sintab,
  emxArray_creal_T *y);
static void r2br_r2dit_trig_impl(const emxArray_creal_T *x, int unsigned_nRows,
  const emxArray_real_T *costab, const emxArray_real_T *sintab, emxArray_creal_T
  *y);
static void repmat(const double a_data[], const int a_size[1], double b_data[],
                   int b_size[1]);
static double sum(const double x_data[], const int x_size[1]);
static void welch_psd(const emxArray_real_T *signals, double window_data[], int
                      window_size[1], double CSM_data[], int CSM_size[2], double
                      frequencies_data[], int frequencies_size[2]);

// Function Definitions

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                const double zi[6]
//                double y_data[]
//                int y_size[1]
// Return Type  : void
//
static void b_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1])
{
  int nx;
  int k;
  int naxpy;
  int j;
  static const double dv2[7] = { 0.007167667426659, 0.0, -0.021503002279976, 0.0,
    0.021503002279976, 0.0, -0.007167667426659 };

  double as;
  static const double dv3[7] = { 1.0, -5.0445676794459251, 10.694677001386529,
    -12.217373564180143, 7.9414563333917991, -2.7860101600364189,
    0.411839132950083 };

  y_size[0] = (short)x_size[0];
  nx = x_size[0];
  for (k = 0; k < 6; k++) {
    y_data[k] = zi[k];
  }

  for (k = 7; k <= nx; k++) {
    y_data[k - 1] = 0.0;
  }

  for (k = 1; k <= nx; k++) {
    naxpy = (nx - k) + 1;
    if (!(naxpy < 7)) {
      naxpy = 7;
    }

    for (j = -1; j + 2 <= naxpy; j++) {
      y_data[k + j] += x_data[k - 1] * dv2[j + 1];
    }

    naxpy = nx - k;
    if (!(naxpy < 6)) {
      naxpy = 6;
    }

    as = -y_data[k - 1];
    for (j = 1; j <= naxpy; j++) {
      y_data[(k + j) - 1] += as * dv3[j];
    }
  }
}

//
// Arguments    : const double x_in_data[]
//                const int x_in_size[1]
//                emxArray_real_T *y_out
// Return Type  : void
//
static void b_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out)
{
  int x_size_idx_0;
  int loop_ub;
  double x_data[8000];
  int i2;
  double d2;
  double d3;
  static double y_data[8036];
  int y_size[1];
  double a[6];
  static const double b_a[6] = { -0.0071676674264306165, -0.00716766742758271,
    0.014335334854835756, 0.014335334852045524, -0.0071676674261167816,
    -0.0071676674267530565 };

  static double b_y_data[8036];
  int b_y_size[1];
  int c_y_size[1];
  int tmp_data[8000];
  if (x_in_size[0] == 1) {
    x_size_idx_0 = 1;
    x_data[0] = x_in_data[0];
  } else {
    x_size_idx_0 = x_in_size[0];
    loop_ub = x_in_size[0];
    for (i2 = 0; i2 < loop_ub; i2++) {
      x_data[i2] = x_in_data[i2];
    }
  }

  if (x_size_idx_0 == 0) {
    i2 = y_out->size[0] * y_out->size[1];
    y_out->size[0] = 0;
    y_out->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)y_out, i2, sizeof(double));
  } else {
    d2 = 2.0 * x_data[0];
    d3 = 2.0 * x_data[x_size_idx_0 - 1];
    for (i2 = 0; i2 < 18; i2++) {
      y_data[i2] = d2 - x_data[18 - i2];
    }

    for (i2 = 0; i2 < x_size_idx_0; i2++) {
      y_data[i2 + 18] = x_data[i2];
    }

    for (i2 = 0; i2 < 18; i2++) {
      y_data[(i2 + x_size_idx_0) + 18] = d3 - x_data[(x_size_idx_0 - i2) - 2];
    }

    for (i2 = 0; i2 < 6; i2++) {
      a[i2] = b_a[i2] * y_data[0];
    }

    y_size[0] = 36 + x_size_idx_0;
    loop_ub = 36 + x_size_idx_0;
    for (i2 = 0; i2 < loop_ub; i2++) {
      b_y_data[i2] = y_data[i2];
    }

    b_filter(b_y_data, y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    for (i2 = 0; i2 < 6; i2++) {
      a[i2] = b_a[i2] * y_data[0];
    }

    c_y_size[0] = b_y_size[0];
    loop_ub = b_y_size[0];
    for (i2 = 0; i2 < loop_ub; i2++) {
      b_y_data[i2] = y_data[i2];
    }

    b_filter(b_y_data, c_y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    if (x_in_size[0] == 1) {
      i2 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = 1;
      y_out->size[1] = x_size_idx_0;
      emxEnsureCapacity((emxArray__common *)y_out, i2, sizeof(double));
      for (i2 = 0; i2 < x_size_idx_0; i2++) {
        y_out->data[y_out->size[0] * i2] = y_data[18 + i2];
      }
    } else {
      i2 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = x_size_idx_0;
      y_out->size[1] = 1;
      emxEnsureCapacity((emxArray__common *)y_out, i2, sizeof(double));
      for (i2 = 0; i2 < x_size_idx_0; i2++) {
        tmp_data[i2] = 19 + i2;
      }

      for (i2 = 0; i2 < x_size_idx_0; i2++) {
        y_out->data[i2] = y_data[tmp_data[i2] - 1];
      }
    }
  }
}

//
// Arguments    : const emxArray_creal_T *x
//                int n1_unsigned
//                const emxArray_real_T *costab
//                const emxArray_real_T *sintab
//                emxArray_creal_T *y
// Return Type  : void
//
static void b_r2br_r2dit_trig(const emxArray_creal_T *x, int n1_unsigned, const
  emxArray_real_T *costab, const emxArray_real_T *sintab, emxArray_creal_T *y)
{
  int j;
  int nRowsD2;
  int nRowsD4;
  int iy;
  int iDelta;
  int ix;
  int ju;
  int i;
  boolean_T tst;
  double temp_re;
  double temp_im;
  double twid_re;
  double twid_im;
  int ihi;
  j = x->size[0];
  if (!(j < n1_unsigned)) {
    j = n1_unsigned;
  }

  nRowsD2 = n1_unsigned / 2;
  nRowsD4 = nRowsD2 / 2;
  iy = y->size[0];
  y->size[0] = n1_unsigned;
  emxEnsureCapacity((emxArray__common *)y, iy, sizeof(creal_T));
  if (n1_unsigned > x->size[0]) {
    iDelta = y->size[0];
    iy = y->size[0];
    y->size[0] = iDelta;
    emxEnsureCapacity((emxArray__common *)y, iy, sizeof(creal_T));
    for (iy = 0; iy < iDelta; iy++) {
      y->data[iy].re = 0.0;
      y->data[iy].im = 0.0;
    }
  }

  ix = 0;
  ju = 0;
  iy = 0;
  for (i = 1; i < j; i++) {
    y->data[iy] = x->data[ix];
    iDelta = n1_unsigned;
    tst = true;
    while (tst) {
      iDelta >>= 1;
      ju ^= iDelta;
      tst = ((ju & iDelta) == 0);
    }

    iy = ju;
    ix++;
  }

  y->data[iy] = x->data[ix];
  if (n1_unsigned > 1) {
    for (i = 0; i <= n1_unsigned - 2; i += 2) {
      temp_re = y->data[i + 1].re;
      temp_im = y->data[i + 1].im;
      y->data[i + 1].re = y->data[i].re - y->data[i + 1].re;
      y->data[i + 1].im = y->data[i].im - y->data[i + 1].im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }
  }

  iDelta = 2;
  iy = 4;
  ix = 1 + ((nRowsD4 - 1) << 2);
  while (nRowsD4 > 0) {
    for (i = 0; i < ix; i += iy) {
      temp_re = y->data[i + iDelta].re;
      temp_im = y->data[i + iDelta].im;
      y->data[i + iDelta].re = y->data[i].re - temp_re;
      y->data[i + iDelta].im = y->data[i].im - temp_im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }

    ju = 1;
    for (j = nRowsD4; j < nRowsD2; j += nRowsD4) {
      twid_re = costab->data[j];
      twid_im = sintab->data[j];
      i = ju;
      ihi = ju + ix;
      while (i < ihi) {
        temp_re = twid_re * y->data[i + iDelta].re - twid_im * y->data[i +
          iDelta].im;
        temp_im = twid_re * y->data[i + iDelta].im + twid_im * y->data[i +
          iDelta].re;
        y->data[i + iDelta].re = y->data[i].re - temp_re;
        y->data[i + iDelta].im = y->data[i].im - temp_im;
        y->data[i].re += temp_re;
        y->data[i].im += temp_im;
        i += iy;
      }

      ju++;
    }

    nRowsD4 /= 2;
    iDelta = iy;
    iy += iy;
    ix -= iDelta;
  }
}

//
// Arguments    : double a
//                double varargin_1
//                double b_data[]
//                int b_size[1]
// Return Type  : void
//
static void b_repmat(double a, double varargin_1, double b_data[], int b_size[1])
{
  int loop_ub;
  int i9;
  b_size[0] = (short)(int)varargin_1;
  loop_ub = (short)(int)varargin_1;
  for (i9 = 0; i9 < loop_ub; i9++) {
    b_data[i9] = a;
  }
}

//
// Arguments    : int nRows
//                emxArray_creal_T *wwc
// Return Type  : void
//
static void bluestein_setup(int nRows, emxArray_creal_T *wwc)
{
  int nInt2m1;
  int idx;
  int rt;
  int nInt2;
  int k;
  int y;
  double nt_im;
  double nt_re;
  nInt2m1 = (nRows + nRows) - 1;
  idx = wwc->size[0];
  wwc->size[0] = nInt2m1;
  emxEnsureCapacity((emxArray__common *)wwc, idx, sizeof(creal_T));
  idx = nRows;
  rt = 0;
  wwc->data[nRows - 1].re = 1.0;
  wwc->data[nRows - 1].im = 0.0;
  nInt2 = nRows << 1;
  for (k = 1; k < nRows; k++) {
    y = (k << 1) - 1;
    if (nInt2 - rt <= y) {
      rt += y - nInt2;
    } else {
      rt += y;
    }

    nt_im = -3.1415926535897931 * (double)rt / (double)nRows;
    if (nt_im == 0.0) {
      nt_re = 1.0;
      nt_im = 0.0;
    } else {
      nt_re = std::cos(nt_im);
      nt_im = std::sin(nt_im);
    }

    wwc->data[idx - 2].re = nt_re;
    wwc->data[idx - 2].im = -nt_im;
    idx--;
  }

  idx = 0;
  for (k = nInt2m1 - 1; k >= nRows; k--) {
    wwc->data[k] = wwc->data[idx];
    idx++;
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                const double zi[6]
//                double y_data[]
//                int y_size[1]
// Return Type  : void
//
static void c_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1])
{
  int nx;
  int k;
  int naxpy;
  int j;
  static const double dv4[7] = { 0.00107948982226, 0.0, -0.003238469466779, 0.0,
    0.003238469466779, 0.0, -0.00107948982226 };

  double as;
  static const double dv5[7] = { 1.0, -5.5390031075784583, 12.814616286414871,
    -15.852598393614553, 11.060956098498181, -4.1275431915197149,
    0.64357270671065 };

  y_size[0] = (short)x_size[0];
  nx = x_size[0];
  for (k = 0; k < 6; k++) {
    y_data[k] = zi[k];
  }

  for (k = 7; k <= nx; k++) {
    y_data[k - 1] = 0.0;
  }

  for (k = 1; k <= nx; k++) {
    naxpy = (nx - k) + 1;
    if (!(naxpy < 7)) {
      naxpy = 7;
    }

    for (j = -1; j + 2 <= naxpy; j++) {
      y_data[k + j] += x_data[k - 1] * dv4[j + 1];
    }

    naxpy = nx - k;
    if (!(naxpy < 6)) {
      naxpy = 6;
    }

    as = -y_data[k - 1];
    for (j = 1; j <= naxpy; j++) {
      y_data[(k + j) - 1] += as * dv5[j];
    }
  }
}

//
// Arguments    : const double x_in_data[]
//                const int x_in_size[1]
//                emxArray_real_T *y_out
// Return Type  : void
//
static void c_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out)
{
  int x_size_idx_0;
  int loop_ub;
  double x_data[8000];
  int i3;
  double d4;
  double d5;
  static double y_data[8036];
  int y_size[1];
  double a[6];
  static const double b_a[6] = { -0.0010794898272580762, -0.0010794897995737159,
    0.0021589796031568544, 0.0021589796823893513, -0.0010794898396731503,
    -0.0010794898190433745 };

  static double b_y_data[8036];
  int b_y_size[1];
  int c_y_size[1];
  int tmp_data[8000];
  if (x_in_size[0] == 1) {
    x_size_idx_0 = 1;
    x_data[0] = x_in_data[0];
  } else {
    x_size_idx_0 = x_in_size[0];
    loop_ub = x_in_size[0];
    for (i3 = 0; i3 < loop_ub; i3++) {
      x_data[i3] = x_in_data[i3];
    }
  }

  if (x_size_idx_0 == 0) {
    i3 = y_out->size[0] * y_out->size[1];
    y_out->size[0] = 0;
    y_out->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)y_out, i3, sizeof(double));
  } else {
    d4 = 2.0 * x_data[0];
    d5 = 2.0 * x_data[x_size_idx_0 - 1];
    for (i3 = 0; i3 < 18; i3++) {
      y_data[i3] = d4 - x_data[18 - i3];
    }

    for (i3 = 0; i3 < x_size_idx_0; i3++) {
      y_data[i3 + 18] = x_data[i3];
    }

    for (i3 = 0; i3 < 18; i3++) {
      y_data[(i3 + x_size_idx_0) + 18] = d5 - x_data[(x_size_idx_0 - i3) - 2];
    }

    for (i3 = 0; i3 < 6; i3++) {
      a[i3] = b_a[i3] * y_data[0];
    }

    y_size[0] = 36 + x_size_idx_0;
    loop_ub = 36 + x_size_idx_0;
    for (i3 = 0; i3 < loop_ub; i3++) {
      b_y_data[i3] = y_data[i3];
    }

    c_filter(b_y_data, y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    for (i3 = 0; i3 < 6; i3++) {
      a[i3] = b_a[i3] * y_data[0];
    }

    c_y_size[0] = b_y_size[0];
    loop_ub = b_y_size[0];
    for (i3 = 0; i3 < loop_ub; i3++) {
      b_y_data[i3] = y_data[i3];
    }

    c_filter(b_y_data, c_y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    if (x_in_size[0] == 1) {
      i3 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = 1;
      y_out->size[1] = x_size_idx_0;
      emxEnsureCapacity((emxArray__common *)y_out, i3, sizeof(double));
      for (i3 = 0; i3 < x_size_idx_0; i3++) {
        y_out->data[y_out->size[0] * i3] = y_data[18 + i3];
      }
    } else {
      i3 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = x_size_idx_0;
      y_out->size[1] = 1;
      emxEnsureCapacity((emxArray__common *)y_out, i3, sizeof(double));
      for (i3 = 0; i3 < x_size_idx_0; i3++) {
        tmp_data[i3] = 19 + i3;
      }

      for (i3 = 0; i3 < x_size_idx_0; i3++) {
        y_out->data[i3] = y_data[tmp_data[i3] - 1];
      }
    }
  }
}

//
// Arguments    : const emxArray_creal_T *x
//                int n1_unsigned
//                const emxArray_real_T *costab
//                const emxArray_real_T *sintab
//                emxArray_creal_T *y
// Return Type  : void
//
static void c_r2br_r2dit_trig(const emxArray_creal_T *x, int n1_unsigned, const
  emxArray_real_T *costab, const emxArray_real_T *sintab, emxArray_creal_T *y)
{
  int j;
  int nRowsD2;
  int nRowsD4;
  int iDelta2;
  int iy;
  int ix;
  int ju;
  int i;
  boolean_T tst;
  double temp_re;
  double temp_im;
  double r;
  double twid_im;
  int ihi;
  j = x->size[0];
  if (!(j < n1_unsigned)) {
    j = n1_unsigned;
  }

  nRowsD2 = n1_unsigned / 2;
  nRowsD4 = nRowsD2 / 2;
  iDelta2 = y->size[0];
  y->size[0] = n1_unsigned;
  emxEnsureCapacity((emxArray__common *)y, iDelta2, sizeof(creal_T));
  if (n1_unsigned > x->size[0]) {
    iy = y->size[0];
    iDelta2 = y->size[0];
    y->size[0] = iy;
    emxEnsureCapacity((emxArray__common *)y, iDelta2, sizeof(creal_T));
    for (iDelta2 = 0; iDelta2 < iy; iDelta2++) {
      y->data[iDelta2].re = 0.0;
      y->data[iDelta2].im = 0.0;
    }
  }

  ix = 0;
  ju = 0;
  iy = 0;
  for (i = 1; i < j; i++) {
    y->data[iy] = x->data[ix];
    iDelta2 = n1_unsigned;
    tst = true;
    while (tst) {
      iDelta2 >>= 1;
      ju ^= iDelta2;
      tst = ((ju & iDelta2) == 0);
    }

    iy = ju;
    ix++;
  }

  y->data[iy] = x->data[ix];
  if (n1_unsigned > 1) {
    for (i = 0; i <= n1_unsigned - 2; i += 2) {
      temp_re = y->data[i + 1].re;
      temp_im = y->data[i + 1].im;
      y->data[i + 1].re = y->data[i].re - y->data[i + 1].re;
      y->data[i + 1].im = y->data[i].im - y->data[i + 1].im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }
  }

  iy = 2;
  iDelta2 = 4;
  ix = 1 + ((nRowsD4 - 1) << 2);
  while (nRowsD4 > 0) {
    for (i = 0; i < ix; i += iDelta2) {
      temp_re = y->data[i + iy].re;
      temp_im = y->data[i + iy].im;
      y->data[i + iy].re = y->data[i].re - temp_re;
      y->data[i + iy].im = y->data[i].im - temp_im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }

    ju = 1;
    for (j = nRowsD4; j < nRowsD2; j += nRowsD4) {
      r = costab->data[j];
      twid_im = sintab->data[j];
      i = ju;
      ihi = ju + ix;
      while (i < ihi) {
        temp_re = r * y->data[i + iy].re - twid_im * y->data[i + iy].im;
        temp_im = r * y->data[i + iy].im + twid_im * y->data[i + iy].re;
        y->data[i + iy].re = y->data[i].re - temp_re;
        y->data[i + iy].im = y->data[i].im - temp_im;
        y->data[i].re += temp_re;
        y->data[i].im += temp_im;
        i += iDelta2;
      }

      ju++;
    }

    nRowsD4 /= 2;
    iy = iDelta2;
    iDelta2 += iDelta2;
    ix -= iy;
  }

  if (y->size[0] > 1) {
    r = 1.0 / (double)y->size[0];
    iDelta2 = y->size[0];
    emxEnsureCapacity((emxArray__common *)y, iDelta2, sizeof(creal_T));
    iy = y->size[0];
    for (iDelta2 = 0; iDelta2 < iy; iDelta2++) {
      y->data[iDelta2].re *= r;
      y->data[iDelta2].im *= r;
    }
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                const double zi[6]
//                double y_data[]
//                int y_size[1]
// Return Type  : void
//
static void d_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1])
{
  int nx;
  int k;
  int naxpy;
  int j;
  static const double dv6[7] = { 0.00014931384513637959, 0.0,
    -0.00044794153540913872, 0.0, 0.00044794153540913872, 0.0,
    -0.00014931384513637959 };

  double as;
  static const double dv7[7] = { 1.0, -5.7744885348453057, 13.9024822354029,
    -17.862961622606818, 12.918932118347655, -4.9864648458745462,
    0.802500656483461 };

  y_size[0] = (short)x_size[0];
  nx = x_size[0];
  for (k = 0; k < 6; k++) {
    y_data[k] = zi[k];
  }

  for (k = 7; k <= nx; k++) {
    y_data[k - 1] = 0.0;
  }

  for (k = 1; k <= nx; k++) {
    naxpy = (nx - k) + 1;
    if (!(naxpy < 7)) {
      naxpy = 7;
    }

    for (j = -1; j + 2 <= naxpy; j++) {
      y_data[k + j] += x_data[k - 1] * dv6[j + 1];
    }

    naxpy = nx - k;
    if (!(naxpy < 6)) {
      naxpy = 6;
    }

    as = -y_data[k - 1];
    for (j = 1; j <= naxpy; j++) {
      y_data[(k + j) - 1] += as * dv7[j];
    }
  }
}

//
// Arguments    : const double x_in_data[]
//                const int x_in_size[1]
//                emxArray_real_T *y_out
// Return Type  : void
//
static void d_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out)
{
  int x_size_idx_0;
  int loop_ub;
  double x_data[8000];
  int i4;
  double d6;
  double d7;
  static double y_data[8036];
  int y_size[1];
  double a[6];
  static const double b_a[6] = { -0.00014931381404319587,
    -0.00014931399359042888, 0.00029862797409114432, 0.00029862741867479659,
    -0.00014931371504361222, -0.00014931387008867994 };

  static double b_y_data[8036];
  int b_y_size[1];
  int c_y_size[1];
  int tmp_data[8000];
  if (x_in_size[0] == 1) {
    x_size_idx_0 = 1;
    x_data[0] = x_in_data[0];
  } else {
    x_size_idx_0 = x_in_size[0];
    loop_ub = x_in_size[0];
    for (i4 = 0; i4 < loop_ub; i4++) {
      x_data[i4] = x_in_data[i4];
    }
  }

  if (x_size_idx_0 == 0) {
    i4 = y_out->size[0] * y_out->size[1];
    y_out->size[0] = 0;
    y_out->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)y_out, i4, sizeof(double));
  } else {
    d6 = 2.0 * x_data[0];
    d7 = 2.0 * x_data[x_size_idx_0 - 1];
    for (i4 = 0; i4 < 18; i4++) {
      y_data[i4] = d6 - x_data[18 - i4];
    }

    for (i4 = 0; i4 < x_size_idx_0; i4++) {
      y_data[i4 + 18] = x_data[i4];
    }

    for (i4 = 0; i4 < 18; i4++) {
      y_data[(i4 + x_size_idx_0) + 18] = d7 - x_data[(x_size_idx_0 - i4) - 2];
    }

    for (i4 = 0; i4 < 6; i4++) {
      a[i4] = b_a[i4] * y_data[0];
    }

    y_size[0] = 36 + x_size_idx_0;
    loop_ub = 36 + x_size_idx_0;
    for (i4 = 0; i4 < loop_ub; i4++) {
      b_y_data[i4] = y_data[i4];
    }

    d_filter(b_y_data, y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    for (i4 = 0; i4 < 6; i4++) {
      a[i4] = b_a[i4] * y_data[0];
    }

    c_y_size[0] = b_y_size[0];
    loop_ub = b_y_size[0];
    for (i4 = 0; i4 < loop_ub; i4++) {
      b_y_data[i4] = y_data[i4];
    }

    d_filter(b_y_data, c_y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    if (x_in_size[0] == 1) {
      i4 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = 1;
      y_out->size[1] = x_size_idx_0;
      emxEnsureCapacity((emxArray__common *)y_out, i4, sizeof(double));
      for (i4 = 0; i4 < x_size_idx_0; i4++) {
        y_out->data[y_out->size[0] * i4] = y_data[18 + i4];
      }
    } else {
      i4 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = x_size_idx_0;
      y_out->size[1] = 1;
      emxEnsureCapacity((emxArray__common *)y_out, i4, sizeof(double));
      for (i4 = 0; i4 < x_size_idx_0; i4++) {
        tmp_data[i4] = 19 + i4;
      }

      for (i4 = 0; i4 < x_size_idx_0; i4++) {
        y_out->data[i4] = y_data[tmp_data[i4] - 1];
      }
    }
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                int N2
//                int n1
//                const emxArray_real_T *costab
//                const emxArray_real_T *sintab
//                const emxArray_real_T *sintabinv
//                emxArray_creal_T *y
// Return Type  : void
//
static void dobluesteinfft(const double x_data[], const int x_size[1], int N2,
  int n1, const emxArray_real_T *costab, const emxArray_real_T *sintab, const
  emxArray_real_T *sintabinv, emxArray_creal_T *y)
{
  emxArray_creal_T *wwc;
  int minNrowsNx;
  int k;
  int xidx;
  double wwc_re;
  double wwc_im;
  emxArray_creal_T *fy;
  emxArray_creal_T *fv;
  double fv_re;
  double fv_im;
  double b_wwc_re;
  double b_fv_im;
  double b_wwc_im;
  double b_fv_re;
  emxInit_creal_T(&wwc, 1);
  bluestein_setup(n1, wwc);
  minNrowsNx = x_size[0];
  if (n1 < minNrowsNx) {
    minNrowsNx = n1;
  }

  k = y->size[0];
  y->size[0] = n1;
  emxEnsureCapacity((emxArray__common *)y, k, sizeof(creal_T));
  if (n1 > x_size[0]) {
    xidx = y->size[0];
    k = y->size[0];
    y->size[0] = xidx;
    emxEnsureCapacity((emxArray__common *)y, k, sizeof(creal_T));
    for (k = 0; k < xidx; k++) {
      y->data[k].re = 0.0;
      y->data[k].im = 0.0;
    }
  }

  xidx = 0;
  for (k = 0; k + 1 <= minNrowsNx; k++) {
    wwc_re = wwc->data[(n1 + k) - 1].re;
    wwc_im = wwc->data[(n1 + k) - 1].im;
    y->data[k].re = wwc_re * x_data[xidx];
    y->data[k].im = wwc_im * -x_data[xidx];
    xidx++;
  }

  while (minNrowsNx + 1 <= n1) {
    y->data[minNrowsNx].re = 0.0;
    y->data[minNrowsNx].im = 0.0;
    minNrowsNx++;
  }

  emxInit_creal_T(&fy, 1);
  emxInit_creal_T(&fv, 1);
  r2br_r2dit_trig_impl(y, N2, costab, sintab, fy);
  b_r2br_r2dit_trig(wwc, N2, costab, sintab, fv);
  k = fy->size[0];
  emxEnsureCapacity((emxArray__common *)fy, k, sizeof(creal_T));
  xidx = fy->size[0];
  for (k = 0; k < xidx; k++) {
    wwc_re = fy->data[k].re;
    wwc_im = fy->data[k].im;
    fv_re = fv->data[k].re;
    fv_im = fv->data[k].im;
    fy->data[k].re = wwc_re * fv_re - wwc_im * fv_im;
    fy->data[k].im = wwc_re * fv_im + wwc_im * fv_re;
  }

  c_r2br_r2dit_trig(fy, N2, costab, sintabinv, fv);
  xidx = 0;
  k = n1 - 1;
  emxFree_creal_T(&fy);
  while (k + 1 <= wwc->size[0]) {
    wwc_re = wwc->data[k].re;
    fv_re = fv->data[k].re;
    wwc_im = wwc->data[k].im;
    fv_im = fv->data[k].im;
    b_wwc_re = wwc->data[k].re;
    b_fv_im = fv->data[k].im;
    b_wwc_im = wwc->data[k].im;
    b_fv_re = fv->data[k].re;
    y->data[xidx].re = wwc_re * fv_re + wwc_im * fv_im;
    y->data[xidx].im = b_wwc_re * b_fv_im - b_wwc_im * b_fv_re;
    xidx++;
    k++;
  }

  emxFree_creal_T(&fv);
  emxFree_creal_T(&wwc);
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                const double zi[6]
//                double y_data[]
//                int y_size[1]
// Return Type  : void
//
static void e_filter(const double x_data[], const int x_size[1], const double
                     zi[6], double y_data[], int y_size[1])
{
  int nx;
  int k;
  int naxpy;
  int j;
  static const double dv8[7] = { 1.9675346213925849E-5, 0.0,
    -5.9026038641777543E-5, 0.0, 5.9026038641777543E-5, 0.0,
    -1.9675346213925849E-5 };

  double as;
  static const double dv9[7] = { 1.0, -5.8886044477318116, 14.450542855246466,
    -18.915859691827226, 13.930370002411454, -5.4723100859653524,
    0.895861367980286 };

  y_size[0] = (short)x_size[0];
  nx = x_size[0];
  for (k = 0; k < 6; k++) {
    y_data[k] = zi[k];
  }

  for (k = 7; k <= nx; k++) {
    y_data[k - 1] = 0.0;
  }

  for (k = 1; k <= nx; k++) {
    naxpy = (nx - k) + 1;
    if (!(naxpy < 7)) {
      naxpy = 7;
    }

    for (j = -1; j + 2 <= naxpy; j++) {
      y_data[k + j] += x_data[k - 1] * dv8[j + 1];
    }

    naxpy = nx - k;
    if (!(naxpy < 6)) {
      naxpy = 6;
    }

    as = -y_data[k - 1];
    for (j = 1; j <= naxpy; j++) {
      y_data[(k + j) - 1] += as * dv9[j];
    }
  }
}

//
// Arguments    : const double x_in_data[]
//                const int x_in_size[1]
//                emxArray_real_T *y_out
// Return Type  : void
//
static void e_filtfilt(const double x_in_data[], const int x_in_size[1],
  emxArray_real_T *y_out)
{
  int x_size_idx_0;
  int loop_ub;
  double x_data[8000];
  int i5;
  double d8;
  double d9;
  static double y_data[8036];
  int y_size[1];
  double a[6];
  static const double b_a[6] = { -1.9675619502730023E-5, -1.967401021306225E-5,
    3.9348079257138779E-5, 3.9353248749813825E-5, -1.9676596906123338E-5,
    -1.9675101385043892E-5 };

  static double b_y_data[8036];
  int b_y_size[1];
  int c_y_size[1];
  int tmp_data[8000];
  if (x_in_size[0] == 1) {
    x_size_idx_0 = 1;
    x_data[0] = x_in_data[0];
  } else {
    x_size_idx_0 = x_in_size[0];
    loop_ub = x_in_size[0];
    for (i5 = 0; i5 < loop_ub; i5++) {
      x_data[i5] = x_in_data[i5];
    }
  }

  if (x_size_idx_0 == 0) {
    i5 = y_out->size[0] * y_out->size[1];
    y_out->size[0] = 0;
    y_out->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)y_out, i5, sizeof(double));
  } else {
    d8 = 2.0 * x_data[0];
    d9 = 2.0 * x_data[x_size_idx_0 - 1];
    for (i5 = 0; i5 < 18; i5++) {
      y_data[i5] = d8 - x_data[18 - i5];
    }

    for (i5 = 0; i5 < x_size_idx_0; i5++) {
      y_data[i5 + 18] = x_data[i5];
    }

    for (i5 = 0; i5 < 18; i5++) {
      y_data[(i5 + x_size_idx_0) + 18] = d9 - x_data[(x_size_idx_0 - i5) - 2];
    }

    for (i5 = 0; i5 < 6; i5++) {
      a[i5] = b_a[i5] * y_data[0];
    }

    y_size[0] = 36 + x_size_idx_0;
    loop_ub = 36 + x_size_idx_0;
    for (i5 = 0; i5 < loop_ub; i5++) {
      b_y_data[i5] = y_data[i5];
    }

    e_filter(b_y_data, y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    for (i5 = 0; i5 < 6; i5++) {
      a[i5] = b_a[i5] * y_data[0];
    }

    c_y_size[0] = b_y_size[0];
    loop_ub = b_y_size[0];
    for (i5 = 0; i5 < loop_ub; i5++) {
      b_y_data[i5] = y_data[i5];
    }

    e_filter(b_y_data, c_y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    if (x_in_size[0] == 1) {
      i5 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = 1;
      y_out->size[1] = x_size_idx_0;
      emxEnsureCapacity((emxArray__common *)y_out, i5, sizeof(double));
      for (i5 = 0; i5 < x_size_idx_0; i5++) {
        y_out->data[y_out->size[0] * i5] = y_data[18 + i5];
      }
    } else {
      i5 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = x_size_idx_0;
      y_out->size[1] = 1;
      emxEnsureCapacity((emxArray__common *)y_out, i5, sizeof(double));
      for (i5 = 0; i5 < x_size_idx_0; i5++) {
        tmp_data[i5] = 19 + i5;
      }

      for (i5 = 0; i5 < x_size_idx_0; i5++) {
        y_out->data[i5] = y_data[tmp_data[i5] - 1];
      }
    }
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                emxArray_creal_T *y
// Return Type  : void
//
static void fft(const double x_data[], const int x_size[1], emxArray_creal_T *y)
{
  emxArray_real_T *costab;
  int N2blue;
  emxArray_real_T *sintab;
  emxArray_real_T *sintabinv;
  boolean_T useRadix2;
  int nRows;
  if (x_size[0] == 0) {
    N2blue = y->size[0];
    y->size[0] = 0;
    emxEnsureCapacity((emxArray__common *)y, N2blue, sizeof(creal_T));
  } else {
    emxInit_real_T(&costab, 2);
    emxInit_real_T(&sintab, 2);
    emxInit_real_T(&sintabinv, 2);
    useRadix2 = ((x_size[0] & (x_size[0] - 1)) == 0);
    get_algo_sizes(x_size[0], useRadix2, &N2blue, &nRows);
    generate_twiddle_tables(nRows, useRadix2, costab, sintab, sintabinv);
    if (useRadix2) {
      r2br_r2dit_trig(x_data, x_size, x_size[0], costab, sintab, y);
    } else {
      dobluesteinfft(x_data, x_size, N2blue, x_size[0], costab, sintab,
                     sintabinv, y);
    }

    emxFree_real_T(&sintabinv);
    emxFree_real_T(&sintab);
    emxFree_real_T(&costab);
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                const double zi[6]
//                double y_data[]
//                int y_size[1]
// Return Type  : void
//
static void filter(const double x_data[], const int x_size[1], const double zi[6],
                   double y_data[], int y_size[1])
{
  int nx;
  int k;
  int naxpy;
  int j;
  static const double dv0[7] = { 0.0418768282347742, 0.0, -0.125630484704323,
    0.0, 0.125630484704323, 0.0, -0.0418768282347742 };

  double as;
  static const double dv1[7] = { 1.0, -3.99412602172993, 6.79713743558926,
    -6.44840721730666, 3.65712515526032, -1.17053739881085, 0.159769122451512 };

  y_size[0] = (short)x_size[0];
  nx = x_size[0];
  for (k = 0; k < 6; k++) {
    y_data[k] = zi[k];
  }

  for (k = 7; k <= nx; k++) {
    y_data[k - 1] = 0.0;
  }

  for (k = 1; k <= nx; k++) {
    naxpy = (nx - k) + 1;
    if (!(naxpy < 7)) {
      naxpy = 7;
    }

    for (j = -1; j + 2 <= naxpy; j++) {
      y_data[k + j] += x_data[k - 1] * dv0[j + 1];
    }

    naxpy = nx - k;
    if (!(naxpy < 6)) {
      naxpy = 6;
    }

    as = -y_data[k - 1];
    for (j = 1; j <= naxpy; j++) {
      y_data[(k + j) - 1] += as * dv1[j];
    }
  }
}

//
// Arguments    : const double x_in_data[]
//                const int x_in_size[1]
//                emxArray_real_T *y_out
// Return Type  : void
//
static void filtfilt(const double x_in_data[], const int x_in_size[1],
                     emxArray_real_T *y_out)
{
  int x_size_idx_0;
  int loop_ub;
  double x_data[8000];
  int i1;
  double d0;
  double d1;
  static double y_data[8036];
  int y_size[1];
  double a[6];
  static const double b_a[6] = { -0.041876828234757295, -0.041876828234824783,
    0.083753656469613066, 0.0837536564695041, -0.041876828234757114,
    -0.04187682823477689 };

  static double b_y_data[8036];
  int b_y_size[1];
  int c_y_size[1];
  int tmp_data[8000];
  if (x_in_size[0] == 1) {
    x_size_idx_0 = 1;
    x_data[0] = x_in_data[0];
  } else {
    x_size_idx_0 = x_in_size[0];
    loop_ub = x_in_size[0];
    for (i1 = 0; i1 < loop_ub; i1++) {
      x_data[i1] = x_in_data[i1];
    }
  }

  if (x_size_idx_0 == 0) {
    i1 = y_out->size[0] * y_out->size[1];
    y_out->size[0] = 0;
    y_out->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)y_out, i1, sizeof(double));
  } else {
    d0 = 2.0 * x_data[0];
    d1 = 2.0 * x_data[x_size_idx_0 - 1];
    for (i1 = 0; i1 < 18; i1++) {
      y_data[i1] = d0 - x_data[18 - i1];
    }

    for (i1 = 0; i1 < x_size_idx_0; i1++) {
      y_data[i1 + 18] = x_data[i1];
    }

    for (i1 = 0; i1 < 18; i1++) {
      y_data[(i1 + x_size_idx_0) + 18] = d1 - x_data[(x_size_idx_0 - i1) - 2];
    }

    for (i1 = 0; i1 < 6; i1++) {
      a[i1] = b_a[i1] * y_data[0];
    }

    y_size[0] = 36 + x_size_idx_0;
    loop_ub = 36 + x_size_idx_0;
    for (i1 = 0; i1 < loop_ub; i1++) {
      b_y_data[i1] = y_data[i1];
    }

    filter(b_y_data, y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    for (i1 = 0; i1 < 6; i1++) {
      a[i1] = b_a[i1] * y_data[0];
    }

    c_y_size[0] = b_y_size[0];
    loop_ub = b_y_size[0];
    for (i1 = 0; i1 < loop_ub; i1++) {
      b_y_data[i1] = y_data[i1];
    }

    filter(b_y_data, c_y_size, a, y_data, b_y_size);
    flipud(y_data, b_y_size);
    if (x_in_size[0] == 1) {
      i1 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = 1;
      y_out->size[1] = x_size_idx_0;
      emxEnsureCapacity((emxArray__common *)y_out, i1, sizeof(double));
      for (i1 = 0; i1 < x_size_idx_0; i1++) {
        y_out->data[y_out->size[0] * i1] = y_data[18 + i1];
      }
    } else {
      i1 = y_out->size[0] * y_out->size[1];
      y_out->size[0] = x_size_idx_0;
      y_out->size[1] = 1;
      emxEnsureCapacity((emxArray__common *)y_out, i1, sizeof(double));
      for (i1 = 0; i1 < x_size_idx_0; i1++) {
        tmp_data[i1] = 19 + i1;
      }

      for (i1 = 0; i1 < x_size_idx_0; i1++) {
        y_out->data[i1] = y_data[tmp_data[i1] - 1];
      }
    }
  }
}

//
// Arguments    : double x_data[]
//                int x_size[1]
// Return Type  : void
//
static void flipud(double x_data[], int x_size[1])
{
  int m;
  int md2;
  int i;
  double xtmp;
  m = x_size[0];
  md2 = x_size[0] >> 1;
  for (i = 1; i <= md2; i++) {
    xtmp = x_data[i - 1];
    x_data[i - 1] = x_data[m - i];
    x_data[m - i] = xtmp;
  }
}

//
// Arguments    : int nRows
//                boolean_T useRadix2
//                emxArray_real_T *costab
//                emxArray_real_T *sintab
//                emxArray_real_T *sintabinv
// Return Type  : void
//
static void generate_twiddle_tables(int nRows, boolean_T useRadix2,
  emxArray_real_T *costab, emxArray_real_T *sintab, emxArray_real_T *sintabinv)
{
  double e;
  int nRowsD4;
  static double costab1q_data[8001];
  int nd2;
  int k;
  int n2;
  e = 6.2831853071795862 / (double)nRows;
  nRowsD4 = nRows / 2 / 2;
  costab1q_data[0] = 1.0;
  nd2 = nRowsD4 / 2;
  for (k = 1; k <= nd2; k++) {
    costab1q_data[k] = std::cos(e * (double)k);
  }

  for (k = nd2 + 1; k < nRowsD4; k++) {
    costab1q_data[k] = std::sin(e * (double)(nRowsD4 - k));
  }

  costab1q_data[nRowsD4] = 0.0;
  if (!useRadix2) {
    n2 = nRowsD4 << 1;
    nd2 = costab->size[0] * costab->size[1];
    costab->size[0] = 1;
    costab->size[1] = n2 + 1;
    emxEnsureCapacity((emxArray__common *)costab, nd2, sizeof(double));
    nd2 = sintab->size[0] * sintab->size[1];
    sintab->size[0] = 1;
    sintab->size[1] = n2 + 1;
    emxEnsureCapacity((emxArray__common *)sintab, nd2, sizeof(double));
    costab->data[0] = 1.0;
    sintab->data[0] = 0.0;
    nd2 = sintabinv->size[0] * sintabinv->size[1];
    sintabinv->size[0] = 1;
    sintabinv->size[1] = n2 + 1;
    emxEnsureCapacity((emxArray__common *)sintabinv, nd2, sizeof(double));
    for (k = 1; k <= nRowsD4; k++) {
      sintabinv->data[k] = costab1q_data[nRowsD4 - k];
    }

    for (k = nRowsD4 + 1; k <= n2; k++) {
      sintabinv->data[k] = costab1q_data[k - nRowsD4];
    }

    for (k = 1; k <= nRowsD4; k++) {
      costab->data[k] = costab1q_data[k];
      sintab->data[k] = -costab1q_data[nRowsD4 - k];
    }

    for (k = nRowsD4 + 1; k <= n2; k++) {
      costab->data[k] = -costab1q_data[n2 - k];
      sintab->data[k] = -costab1q_data[k - nRowsD4];
    }
  } else {
    n2 = nRowsD4 << 1;
    nd2 = costab->size[0] * costab->size[1];
    costab->size[0] = 1;
    costab->size[1] = n2 + 1;
    emxEnsureCapacity((emxArray__common *)costab, nd2, sizeof(double));
    nd2 = sintab->size[0] * sintab->size[1];
    sintab->size[0] = 1;
    sintab->size[1] = n2 + 1;
    emxEnsureCapacity((emxArray__common *)sintab, nd2, sizeof(double));
    costab->data[0] = 1.0;
    sintab->data[0] = 0.0;
    for (k = 1; k <= nRowsD4; k++) {
      costab->data[k] = costab1q_data[k];
      sintab->data[k] = -costab1q_data[nRowsD4 - k];
    }

    for (k = nRowsD4 + 1; k <= n2; k++) {
      costab->data[k] = -costab1q_data[n2 - k];
      sintab->data[k] = -costab1q_data[k - nRowsD4];
    }

    nd2 = sintabinv->size[0] * sintabinv->size[1];
    sintabinv->size[0] = 1;
    sintabinv->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)sintabinv, nd2, sizeof(double));
  }
}

//
// Arguments    : int n1
//                boolean_T useRadix2
//                int *N2blue
//                int *nRows
// Return Type  : void
//
static void get_algo_sizes(int n1, boolean_T useRadix2, int *N2blue, int *nRows)
{
  int nn1m1;
  int pmax;
  int pmin;
  boolean_T exitg1;
  int p;
  int pow2p;
  *N2blue = 1;
  if (useRadix2) {
    *nRows = n1;
  } else {
    nn1m1 = (n1 + n1) - 1;
    pmax = 31;
    if (nn1m1 <= 1) {
      pmax = 0;
    } else {
      pmin = 0;
      exitg1 = false;
      while ((!exitg1) && (pmax - pmin > 1)) {
        p = (pmin + pmax) >> 1;
        pow2p = 1 << p;
        if (pow2p == nn1m1) {
          pmax = p;
          exitg1 = true;
        } else if (pow2p > nn1m1) {
          pmax = p;
        } else {
          pmin = p;
        }
      }
    }

    *N2blue = 1 << pmax;
    *nRows = *N2blue;
  }
}

//
// Calculate the generalized cosine window samples
//  x is the length of the window
// Arguments    : double x
//                double w_data[]
//                int w_size[1]
// Return Type  : void
//
static void hannWin(double x, double w_data[], int w_size[1])
{
  int y_size_idx_1;
  int loop_ub;
  int i6;
  int y_data[8000];
  if (x - 1.0 < 0.0) {
    y_size_idx_1 = 0;
  } else {
    y_size_idx_1 = (int)std::floor(x - 1.0) + 1;
    loop_ub = (int)std::floor(x - 1.0);
    for (i6 = 0; i6 <= loop_ub; i6++) {
      y_data[i6] = i6;
    }
  }

  w_size[0] = y_size_idx_1;
  for (i6 = 0; i6 < y_size_idx_1; i6++) {
    w_data[i6] = 6.2831853071795862 * (double)y_data[i6] / (x - 1.0);
  }

  for (loop_ub = 0; loop_ub + 1 <= y_size_idx_1; loop_ub++) {
    w_data[loop_ub] = std::cos(w_data[loop_ub]);
  }

  for (i6 = 0; i6 < y_size_idx_1; i6++) {
    w_data[i6] = 0.5 * (1.0 - w_data[i6]);
  }
}

//
// Arguments    : const emxArray_real_T *x
// Return Type  : double
//
static double mean(const emxArray_real_T *x)
{
  double y;
  int k;
  if (x->size[0] == 0) {
    y = 0.0;
  } else {
    y = x->data[0];
    for (k = 2; k <= x->size[0]; k++) {
      y += x->data[k - 1];
    }
  }

  y /= (double)x->size[0];
  return y;
}

//
// Arguments    : const double a_data[]
//                const int a_size[1]
//                double y_data[]
//                int y_size[1]
// Return Type  : void
//
static void power(const double a_data[], const int a_size[1], double y_data[],
                  int y_size[1])
{
  int k;
  y_size[0] = (short)a_size[0];
  for (k = 0; k + 1 <= a_size[0]; k++) {
    y_data[k] = a_data[k] * a_data[k];
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
//                int n1_unsigned
//                const emxArray_real_T *costab
//                const emxArray_real_T *sintab
//                emxArray_creal_T *y
// Return Type  : void
//
static void r2br_r2dit_trig(const double x_data[], const int x_size[1], int
  n1_unsigned, const emxArray_real_T *costab, const emxArray_real_T *sintab,
  emxArray_creal_T *y)
{
  int j;
  int nRowsD2;
  int nRowsD4;
  int iy;
  int iDelta;
  int ix;
  int ju;
  int i;
  boolean_T tst;
  double temp_re;
  double temp_im;
  double twid_re;
  double twid_im;
  int ihi;
  j = x_size[0];
  if (!(j < n1_unsigned)) {
    j = n1_unsigned;
  }

  nRowsD2 = n1_unsigned / 2;
  nRowsD4 = nRowsD2 / 2;
  iy = y->size[0];
  y->size[0] = n1_unsigned;
  emxEnsureCapacity((emxArray__common *)y, iy, sizeof(creal_T));
  if (n1_unsigned > x_size[0]) {
    iDelta = y->size[0];
    iy = y->size[0];
    y->size[0] = iDelta;
    emxEnsureCapacity((emxArray__common *)y, iy, sizeof(creal_T));
    for (iy = 0; iy < iDelta; iy++) {
      y->data[iy].re = 0.0;
      y->data[iy].im = 0.0;
    }
  }

  ix = 0;
  ju = 0;
  iy = 0;
  for (i = 1; i < j; i++) {
    y->data[iy].re = x_data[ix];
    y->data[iy].im = 0.0;
    iDelta = n1_unsigned;
    tst = true;
    while (tst) {
      iDelta >>= 1;
      ju ^= iDelta;
      tst = ((ju & iDelta) == 0);
    }

    iy = ju;
    ix++;
  }

  y->data[iy].re = x_data[ix];
  y->data[iy].im = 0.0;
  if (n1_unsigned > 1) {
    for (i = 0; i <= n1_unsigned - 2; i += 2) {
      temp_re = y->data[i + 1].re;
      temp_im = y->data[i + 1].im;
      y->data[i + 1].re = y->data[i].re - y->data[i + 1].re;
      y->data[i + 1].im = y->data[i].im - y->data[i + 1].im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }
  }

  iDelta = 2;
  iy = 4;
  ix = 1 + ((nRowsD4 - 1) << 2);
  while (nRowsD4 > 0) {
    for (i = 0; i < ix; i += iy) {
      temp_re = y->data[i + iDelta].re;
      temp_im = y->data[i + iDelta].im;
      y->data[i + iDelta].re = y->data[i].re - temp_re;
      y->data[i + iDelta].im = y->data[i].im - temp_im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }

    ju = 1;
    for (j = nRowsD4; j < nRowsD2; j += nRowsD4) {
      twid_re = costab->data[j];
      twid_im = sintab->data[j];
      i = ju;
      ihi = ju + ix;
      while (i < ihi) {
        temp_re = twid_re * y->data[i + iDelta].re - twid_im * y->data[i +
          iDelta].im;
        temp_im = twid_re * y->data[i + iDelta].im + twid_im * y->data[i +
          iDelta].re;
        y->data[i + iDelta].re = y->data[i].re - temp_re;
        y->data[i + iDelta].im = y->data[i].im - temp_im;
        y->data[i].re += temp_re;
        y->data[i].im += temp_im;
        i += iy;
      }

      ju++;
    }

    nRowsD4 /= 2;
    iDelta = iy;
    iy += iy;
    ix -= iDelta;
  }
}

//
// Arguments    : const emxArray_creal_T *x
//                int unsigned_nRows
//                const emxArray_real_T *costab
//                const emxArray_real_T *sintab
//                emxArray_creal_T *y
// Return Type  : void
//
static void r2br_r2dit_trig_impl(const emxArray_creal_T *x, int unsigned_nRows,
  const emxArray_real_T *costab, const emxArray_real_T *sintab, emxArray_creal_T
  *y)
{
  int j;
  int nRowsD2;
  int nRowsD4;
  int iy;
  int iDelta;
  int ix;
  int ju;
  int i;
  boolean_T tst;
  double temp_re;
  double temp_im;
  double twid_re;
  double twid_im;
  int ihi;
  j = x->size[0];
  if (!(j < unsigned_nRows)) {
    j = unsigned_nRows;
  }

  nRowsD2 = unsigned_nRows / 2;
  nRowsD4 = nRowsD2 / 2;
  iy = y->size[0];
  y->size[0] = unsigned_nRows;
  emxEnsureCapacity((emxArray__common *)y, iy, sizeof(creal_T));
  if (unsigned_nRows > x->size[0]) {
    iDelta = y->size[0];
    iy = y->size[0];
    y->size[0] = iDelta;
    emxEnsureCapacity((emxArray__common *)y, iy, sizeof(creal_T));
    for (iy = 0; iy < iDelta; iy++) {
      y->data[iy].re = 0.0;
      y->data[iy].im = 0.0;
    }
  }

  ix = 0;
  ju = 0;
  iy = 0;
  for (i = 1; i < j; i++) {
    y->data[iy] = x->data[ix];
    iDelta = unsigned_nRows;
    tst = true;
    while (tst) {
      iDelta >>= 1;
      ju ^= iDelta;
      tst = ((ju & iDelta) == 0);
    }

    iy = ju;
    ix++;
  }

  y->data[iy] = x->data[ix];
  if (unsigned_nRows > 1) {
    for (i = 0; i <= unsigned_nRows - 2; i += 2) {
      temp_re = y->data[i + 1].re;
      temp_im = y->data[i + 1].im;
      y->data[i + 1].re = y->data[i].re - y->data[i + 1].re;
      y->data[i + 1].im = y->data[i].im - y->data[i + 1].im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }
  }

  iDelta = 2;
  iy = 4;
  ix = 1 + ((nRowsD4 - 1) << 2);
  while (nRowsD4 > 0) {
    for (i = 0; i < ix; i += iy) {
      temp_re = y->data[i + iDelta].re;
      temp_im = y->data[i + iDelta].im;
      y->data[i + iDelta].re = y->data[i].re - temp_re;
      y->data[i + iDelta].im = y->data[i].im - temp_im;
      y->data[i].re += temp_re;
      y->data[i].im += temp_im;
    }

    ju = 1;
    for (j = nRowsD4; j < nRowsD2; j += nRowsD4) {
      twid_re = costab->data[j];
      twid_im = sintab->data[j];
      i = ju;
      ihi = ju + ix;
      while (i < ihi) {
        temp_re = twid_re * y->data[i + iDelta].re - twid_im * y->data[i +
          iDelta].im;
        temp_im = twid_re * y->data[i + iDelta].im + twid_im * y->data[i +
          iDelta].re;
        y->data[i + iDelta].re = y->data[i].re - temp_re;
        y->data[i + iDelta].im = y->data[i].im - temp_im;
        y->data[i].re += temp_re;
        y->data[i].im += temp_im;
        i += iy;
      }

      ju++;
    }

    nRowsD4 /= 2;
    iDelta = iy;
    iy += iy;
    ix -= iDelta;
  }
}

//
// Arguments    : const double a_data[]
//                const int a_size[1]
//                double b_data[]
//                int b_size[1]
// Return Type  : void
//
static void repmat(const double a_data[], const int a_size[1], double b_data[],
                   int b_size[1])
{
  int k;
  b_size[0] = (short)a_size[0];
  if ((!(a_size[0] == 0)) && (!((short)a_size[0] == 0))) {
    for (k = 0; k + 1 <= a_size[0]; k++) {
      b_data[k] = a_data[k];
    }
  }
}

//
// Arguments    : const double x_data[]
//                const int x_size[1]
// Return Type  : double
//
static double sum(const double x_data[], const int x_size[1])
{
  double y;
  int k;
  if (x_size[0] == 0) {
    y = 0.0;
  } else {
    y = x_data[0];
    for (k = 2; k <= x_size[0]; k++) {
      y += x_data[k - 1];
    }
  }

  return y;
}

//
// if size(signals,2) > size(signals,1)
//      signals = signals.';
//  end
// Arguments    : const emxArray_real_T *signals
//                double window_data[]
//                int window_size[1]
//                double CSM_data[]
//                int CSM_size[2]
//                double frequencies_data[]
//                int frequencies_size[2]
// Return Type  : void
//
static void welch_psd(const emxArray_real_T *signals, double window_data[], int
                      window_size[1], double CSM_data[], int CSM_size[2], double
                      frequencies_data[], int frequencies_size[2])
{
  int window;
  double y;
  int y_size_idx_1;
  int loop_ub;
  int i7;
  int y_data[8000];
  double back_shift;
  double z;
  double number_of_blocks;
  static double data_taper_data[8000];
  int data_taper_size[1];
  double S_data[4000];
  int a;
  emxArray_real_T *Data_Block;
  emxArray_creal_T *b_Data_Block;
  emxArray_creal_T *c_Data_Block;
  double B;
  double Data_Block_im;
  static double tmp_data[8000];
  int i8;
  short b_tmp_data[4000];
  double P_data[4000];

  //  Function for spectra estimation by Welch's method
  //  Developed by Luiz A. Baccala, Fl?vio Caduda and Luciano Caldas, all from
  //  Escola Polit?cnica - Poli-USP, with cooperation of Carlos Pagani and Felipe 
  //  Amaral from Escola de Engenharia de S?o Carlos - EESC-USP.
  //
  //  Cross-spectra matrix are estimated by Welch's method with 50% overlap and
  //  the window energy loss are compasated by a factor of 1/sum(Wi.^2) where
  //  Wi are the elements of the window [1]. Then, the spectra becomes:
  //  Sxy = fft(x)*conj(fft(y))/sum(Wi.^2)
  //
  //  Code was tested with a known- spectra signal from a white noise filtered
  //  by a filter. The variance (power) of the signal checks with the integral
  //  of the PSD estimated.
  //
  //  INPUT:
  //  -- signals: matrix of signals to perform the spectra estimatino. Size is
  //  [Samples x number of sensors];
  //  -- fs: samplerate in Hertz;
  //  -- window: data taper desired. Must be a vector. For best performance it
  //  should be a power of 2. For general applications do: window=hanning(1024); 
  //
  //  OUTPUT:
  //  -- CSM: Cross Spectral Matrix: Unilateral (0:fs/2) spectra. Welch's
  //  method is used with 50% overlap. Matrix size: sensors x sensors x
  //  windowsize/2
  //  -- frequencies: vector with all frequencies corresponding to each layer
  //  (3rd layer in depth) of CSM.
  //
  //  LAST REVISION: Aug - 18 - 2016
  //  ADDED 'fs' missing term in line 82, for calibration factor
  //  [1] Trobs,M.; Heinzel,G. "Improved spectrum estimation from digitized
  //  time series on a logarithmic frequency axis"
  //  doi:10.1016/j.measurement.2005.10.010
  window = window_size[0];
  y = (double)window / 2.0;
  if (y - 1.0 < 0.0) {
    y_size_idx_1 = 0;
  } else {
    y_size_idx_1 = (int)std::floor(y - 1.0) + 1;
    loop_ub = (int)std::floor(y - 1.0);
    for (i7 = 0; i7 <= loop_ub; i7++) {
      y_data[i7] = i7;
    }
  }

  frequencies_size[0] = 1;
  frequencies_size[1] = y_size_idx_1;
  for (i7 = 0; i7 < y_size_idx_1; i7++) {
    frequencies_data[i7] = (double)y_data[i7] * 250.0 / (double)window_size[0];
  }

  // must be even, best if 2^n
  back_shift = (double)window / 2.0;

  // ORIGINAL;
  z = 2.0 * (double)(signals->size[0] * signals->size[1]) / (double)window;
  number_of_blocks = std::floor(z) - 1.0;
  repmat(window_data, window_size, data_taper_data, data_taper_size);

  //  Data segmentation into blocks of size block_samples:
  y = (double)window / 2.0;
  loop_ub = (int)y;
  for (i7 = 0; i7 < loop_ub; i7++) {
    S_data[i7] = 0.0;
  }

  // ORIGINAL
  //  S = zeros(ceil(block_samples/2),number_of_signals.^2);
  a = 0;
  emxInit_real_T1(&Data_Block, 1);
  emxInit_creal_T(&b_Data_Block, 1);
  emxInit_creal_T(&c_Data_Block, 1);
  while (a <= (int)number_of_blocks - 1) {
    //  Retrieve current data block
    B = ((1.0 + (double)a) - 1.0) * back_shift + 1.0;
    Data_Block_im = (double)window + ((1.0 + (double)a) - 1.0) * back_shift;
    if (B > Data_Block_im) {
      i7 = 0;
      i8 = 0;
    } else {
      i7 = (int)B - 1;
      i8 = (int)Data_Block_im;
    }

    y_size_idx_1 = Data_Block->size[0];
    Data_Block->size[0] = i8 - i7;
    emxEnsureCapacity((emxArray__common *)Data_Block, y_size_idx_1, sizeof
                      (double));
    loop_ub = i8 - i7;
    for (i8 = 0; i8 < loop_ub; i8++) {
      Data_Block->data[i8] = signals->data[i7 + i8];
    }

    b_repmat(mean(Data_Block), (double)window, tmp_data, data_taper_size);
    i7 = Data_Block->size[0];
    emxEnsureCapacity((emxArray__common *)Data_Block, i7, sizeof(double));
    loop_ub = Data_Block->size[0];
    for (i7 = 0; i7 < loop_ub; i7++) {
      Data_Block->data[i7] -= tmp_data[i7];
    }

    i7 = Data_Block->size[0];
    emxEnsureCapacity((emxArray__common *)Data_Block, i7, sizeof(double));
    loop_ub = Data_Block->size[0];
    for (i7 = 0; i7 < loop_ub; i7++) {
      Data_Block->data[i7] *= data_taper_data[i7];
    }

    // Taper it
    fft(Data_Block->data, Data_Block->size, b_Data_Block);

    // FFT it,
    //  bilateral DFT
    //  viii
    B = (double)window / 2.0;
    if (1.0 > B) {
      loop_ub = 0;
    } else {
      loop_ub = (int)B;
    }

    i7 = c_Data_Block->size[0];
    c_Data_Block->size[0] = loop_ub;
    emxEnsureCapacity((emxArray__common *)c_Data_Block, i7, sizeof(creal_T));
    for (i7 = 0; i7 < loop_ub; i7++) {
      c_Data_Block->data[i7] = b_Data_Block->data[i7];
    }

    i7 = b_Data_Block->size[0];
    b_Data_Block->size[0] = c_Data_Block->size[0];
    emxEnsureCapacity((emxArray__common *)b_Data_Block, i7, sizeof(creal_T));
    loop_ub = c_Data_Block->size[0];
    for (i7 = 0; i7 < loop_ub; i7++) {
      b_Data_Block->data[i7] = c_Data_Block->data[i7];
    }

    // ORIGINAL
    //  Data_Block = Data_Block(1:ceil(block_samples/2),:);
    // All spectral combinations:
    loop_ub = (int)((double)window / 2.0);
    for (i7 = 0; i7 < loop_ub; i7++) {
      P_data[i7] = 0.0;
    }

    // ORIGINAL
    //  P = zeros(ceil(block_samples/2)/2,number_of_signals.^2);
    //  THIS IS FOR WIND TUNNEL EESC-USP BEAMFORMING CODE
    //              P(:,c) = real(Data_Block(:,b).*conj(Data_Block(:,aa)));
    //  P(:,c) = Data_Block(:,b).*conj(Data_Block(:,aa));
    //  IS FOR FAN RIG BEAMFORMING CODE
    loop_ub = b_Data_Block->size[0] - 1;
    for (i7 = 0; i7 <= loop_ub; i7++) {
      B = b_Data_Block->data[i7].re;
      Data_Block_im = -b_Data_Block->data[i7].im;
      B = b_Data_Block->data[i7].re * B - b_Data_Block->data[i7].im *
        Data_Block_im;
      P_data[i7] = B;
    }

    //  P(:,c) = Data_Block(:,aa).*conj(Data_Block(:,b)); % THIS IS THE ORIGINAL LINE 
    //  Sum the spectrums up ...
    loop_ub = (int)y;
    for (i7 = 0; i7 < loop_ub; i7++) {
      S_data[i7] += P_data[i7];
    }

    a++;
  }

  emxFree_creal_T(&c_Data_Block);
  emxFree_creal_T(&b_Data_Block);
  emxFree_real_T(&Data_Block);
  power(window_data, window_size, tmp_data, data_taper_size);
  B = sum(tmp_data, data_taper_size) * 250.0 * (std::floor(z) - 1.0);
  loop_ub = (int)y;
  for (i7 = 0; i7 < loop_ub; i7++) {
    S_data[i7] = S_data[i7] * 2.0 / B;
  }

  //  Average them out
  CSM_size[0] = 1;
  CSM_size[1] = (int)y;
  loop_ub = (int)y;
  for (i7 = 0; i7 < loop_ub; i7++) {
    CSM_data[i7] = 0.0;
  }

  //  for a = 1:sensors
  loop_ub = (short)((short)(int)y - 1);
  for (i7 = 0; i7 <= loop_ub; i7++) {
    b_tmp_data[i7] = (short)i7;
  }

  loop_ub = (int)y;
  for (i7 = 0; i7 < loop_ub; i7++) {
    P_data[i7] = S_data[i7];
  }

  loop_ub = (short)((short)(int)y - 1) + 1;
  for (i7 = 0; i7 < loop_ub; i7++) {
    CSM_data[b_tmp_data[i7]] = P_data[i7];
  }

  //  end
  //  clear S
  CSM_data[0] = (CSM_data[0] + CSM_data[0]) - CSM_data[0];
}

//
// Make sure vectorized??
// Arguments    : const double X1_data[]
//                const int X1_size[1]
//                const double X2_data[]
//                const int X2_size[1]
//                double Fs
//                double PSD_data[]
//                int PSD_size[2]
// Return Type  : void
//
void extractPowerSpectrum(const double X1_data[], const int X1_size[1], const
  double X2_data[], const int X2_size[1], double Fs, double PSD_data[], int
  PSD_size[2])
{
  emxArray_real_T *fch1;
  int i0;
  int loop_ub;
  emxArray_real_T *fch2;
  static double hW_data[8000];
  int hW_size[1];
  int b_hW_size[1];
  static double b_hW_data[8000];
  static double PSD1_data[4000];
  int PSD1_size[2];
  double unusedU0_data[4000];
  int unusedU0_size[2];
  double unusedU1_data[4000];
  int unusedU1_size[2];
  emxInit_real_T(&fch1, 2);
  i0 = fch1->size[0] * fch1->size[1];
  fch1->size[0] = 1;
  fch1->size[1] = X1_size[0];
  emxEnsureCapacity((emxArray__common *)fch1, i0, sizeof(double));
  loop_ub = X1_size[0];
  for (i0 = 0; i0 < loop_ub; i0++) {
    fch1->data[i0] = 0.0;
  }

  emxInit_real_T(&fch2, 2);
  i0 = fch2->size[0] * fch2->size[1];
  fch2->size[0] = 1;
  fch2->size[1] = X2_size[0];
  emxEnsureCapacity((emxArray__common *)fch2, i0, sizeof(double));
  loop_ub = X2_size[0];
  for (i0 = 0; i0 < loop_ub; i0++) {
    fch2->data[i0] = 0.0;
  }

  if (Fs == 250.0) {
    filtfilt(X1_data, X1_size, fch1);
    filtfilt(X2_data, X2_size, fch2);
  } else if (Fs == 500.0) {
    b_filtfilt(X1_data, X1_size, fch1);
    b_filtfilt(X2_data, X2_size, fch2);
  } else if (Fs == 1000.0) {
    c_filtfilt(X1_data, X1_size, fch1);
    c_filtfilt(X2_data, X2_size, fch2);
  } else if (Fs == 2000.0) {
    d_filtfilt(X1_data, X1_size, fch1);
    d_filtfilt(X2_data, X2_size, fch2);
  } else {
    if (Fs == 4000.0) {
      e_filtfilt(X1_data, X1_size, fch1);
      e_filtfilt(X2_data, X2_size, fch2);
    }
  }

  hannWin((double)X1_size[0], hW_data, hW_size);
  b_hW_size[0] = hW_size[0];
  loop_ub = hW_size[0];
  for (i0 = 0; i0 < loop_ub; i0++) {
    b_hW_data[i0] = hW_data[i0];
  }

  welch_psd(fch1, b_hW_data, b_hW_size, PSD1_data, PSD1_size, unusedU0_data,
            unusedU0_size);

  // fPSD
  welch_psd(fch2, hW_data, hW_size, unusedU0_data, unusedU0_size, unusedU1_data,
            unusedU1_size);
  PSD_size[0] = 1;
  PSD_size[1] = PSD1_size[1] + unusedU0_size[1];
  loop_ub = PSD1_size[1];
  emxFree_real_T(&fch2);
  emxFree_real_T(&fch1);
  for (i0 = 0; i0 < loop_ub; i0++) {
    PSD_data[i0] = PSD1_data[PSD1_size[0] * i0];
  }

  loop_ub = unusedU0_size[1];
  for (i0 = 0; i0 < loop_ub; i0++) {
    PSD_data[i0 + PSD1_size[1]] = unusedU0_data[unusedU0_size[0] * i0];
  }
}

//
// Arguments    : void
// Return Type  : void
//
void extractPowerSpectrum_initialize()
{
  rt_InitInfAndNaN(8U);
}

//
// Arguments    : void
// Return Type  : void
//
void extractPowerSpectrum_terminate()
{
  // (no terminate code required)
}

//
// File trailer for extractPowerSpectrum.cpp
//
// [EOF]
//
