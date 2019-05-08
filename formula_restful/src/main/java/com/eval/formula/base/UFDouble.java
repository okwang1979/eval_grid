/*      */
package com.eval.formula.base;
/*      */
/*      */

import java.io.Serializable;
/*      */ import java.math.BigDecimal;
/*      */ import java.math.MathContext;
/*      */ import java.math.RoundingMode;
/*      */ import java.util.Arrays;
/*      */ import java.util.StringTokenizer;

/*      */
/*      */ public class UFDouble extends Number
        /*      */ implements Serializable, Comparable
        /*      */ {
    /*      */   static final long serialVersionUID = -809396813980155342L;
    /*      */   private int power;
    /*      */   public static final int ROUND_UP = 0;
    /*      */   public static final int ROUND_DOWN = 1;
    /*      */   public static final int ROUND_CEILING = 2;
    /*      */   public static final int ROUND_FLOOR = 3;
    /*      */   public static final int ROUND_HALF_UP = 4;
    /*      */   public static final int ROUND_HALF_DOWN = 5;
    /*      */   public static final int ROUND_HALF_EVEN = 6;
    /*      */   public static final int ROUND_UNNECESSARY = 7;
    /*      */   private static final int ARRAY_LENGTH = 5;
    /*      */   private static final int EFFICIENCY_SEATE = 16;
    /*      */   private static final long MAX_ONELONG_VALUE = 10000000000000000L;
    /*  108 */   private static final long[] POWER_ARRAY = new long[18];
    /*      */   public static final int ROUND_TO_ZERO_AND_HALF = 8;
    /*      */   private byte si;
    /*      */   private long[] v;
    /*      */   public static UFDouble ONE_DBL;
    /*      */   public static UFDouble ZERO_DBL;
    /*      */   public static final int DEFAULT_POWER = -8;
    /*      */   private boolean trimZero;

    /*      */
    /*      */
    public UFDouble()
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(double d)
    /*      */     throws NumberFormatException
    /*      */ {
        /*  133 */
        this(d, -8);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(double d, int newPower)
    /*      */     throws NumberFormatException
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
        /*  141 */
        setValue(d, newPower);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(int d)
    /*      */ {
        /*  151 */
        this((double) d);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(int d, int pow)
    /*      */ {
        /*  163 */
        this((double) d, pow);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(long d) {
        /*  167 */
        this(d, -8);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(long d, int pow) throws NumberFormatException {
        /*  171 */
        this(d + 0.0D, pow);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(long[] dv, byte si, int pow)
    /*      */     throws NumberFormatException
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
        /*  183 */
        if ((dv == null) || (dv.length != 5)) {
            /*  184 */
            throw new NumberFormatException("array length must be 5");
            /*      */
        }
        /*  186 */
        this.v = dv;
        /*  187 */
        this.si = si;
        /*  188 */
        this.power = pow;
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(Double d)
    /*      */     throws NumberFormatException
    /*      */ {
        /*  198 */
        this(d.doubleValue(), -8);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(String str)
    /*      */     throws NumberFormatException
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
        /*  208 */
        initByString(str);
        /*      */
    }

    /*      */
    /*      */
    private void initByString(String str) {
        /*  212 */
        String s = "";
        /*  213 */
        int npower = -8;
        /*  214 */
        if ((str == null) || (str.trim().length() == 0)) {
            /*  215 */
            s = "0";
            /*      */
        } else {
            /*  217 */
            StringTokenizer token = new StringTokenizer(str, ",");
            /*      */
            /*  219 */
            while (token.hasMoreElements()) {
                /*  220 */
                s = s + token.nextElement().toString();
                /*      */
            }
            /*      */
            /*  223 */
            int pos = s.indexOf(101);
            /*  224 */
            pos = (pos < 0) ? s.indexOf(69) : pos;
            /*  225 */
            if (pos >= 0) {
                /*      */
                try {
                    /*  227 */
                    npower = Integer.parseInt(s.substring(pos + 1));
                    /*      */
                } catch (Throwable t) {
                    /*  229 */
                    npower = -8;
                    /*      */
                }
                /*  231 */
                npower = getEPower(s, npower, pos);
                /*  232 */
                setValue(Double.parseDouble(s), npower);
                /*  233 */
                return;
                /*      */
            }
            /*  235 */
            if (s.charAt(0) == '-') {
                /*  236 */
                this.si = -1;
                /*  237 */
                s = s.substring(1);
                /*  238 */
            } else if (s.charAt(0) == '+') {
                /*  239 */
                s = s.substring(1);
                /*      */
            }
            /*      */
        }
        /*  242 */
        int loc = s.indexOf(46);
        /*      */
        /*  244 */
        if (loc >= 0)
            /*  245 */ npower = s.length() - (loc + 1);
            /*      */
        else {
            /*  247 */
            npower = 0;
            /*      */
        }
        /*      */
        /*  250 */
        fromString(npower, s);
        /*      */
    }

    /*      */
    /*      */
    private int getEPower(String s, int ePower, int eindex) {
        /*  254 */
        int decimalIndex = s.indexOf(".");
        /*  255 */
        boolean hasDecimalDot = decimalIndex > 0;
        /*  256 */
        int revisePower = 0;
        /*  257 */
        if (hasDecimalDot) {
            /*  258 */
            int decimaDigits = eindex - decimalIndex - 1;
            /*  259 */
            if (ePower > 0) {
                /*  260 */
                revisePower = (ePower - decimaDigits >= 0) ? 0 : ePower - decimaDigits;
                /*      */
            }
            /*      */
            else
                /*  263 */         revisePower = ePower - decimaDigits;
            /*      */
        }
        /*      */
        else {
            /*  266 */
            revisePower = (ePower >= 0) ? 0 : ePower;
            /*      */
        }
        /*  268 */
        return revisePower;
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(String str, int newPower)
    /*      */     throws NumberFormatException
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
        /*  278 */
        String s = "";
        /*  279 */
        if ((str == null) || (str.trim().length() == 0)) {
            /*  280 */
            s = "0";
            /*      */
        } else {
            /*  282 */
            StringTokenizer token = new StringTokenizer(str, ",");
            /*      */
            /*  284 */
            while (token.hasMoreElements()) {
                /*  285 */
                s = s + token.nextElement().toString();
                /*      */
            }
            /*  287 */
            if ((s.indexOf(101) >= 0) || (s.indexOf(69) >= 0)) {
                /*  288 */
                setValue(Double.parseDouble(s), getValidPower(newPower));
                /*  289 */
                return;
                /*      */
            }
            /*  291 */
            if (s.charAt(0) == '-') {
                /*  292 */
                this.si = -1;
                /*  293 */
                s = s.substring(1);
                /*  294 */
            } else if (s.charAt(0) == '+') {
                /*  295 */
                s = s.substring(1);
            }
            /*      */
        }
        /*  297 */
        fromString(newPower, s);
        /*      */
    }

    /*      */
    /*      */
    private void fromString(int newPower, String s) {
        /*  301 */
        newPower = getValidPower(newPower);
        /*  302 */
        int loc = s.indexOf(46);
        /*  303 */
        if (loc >= 0) {
            /*  304 */
            String s1 = s.substring(loc + 1);
            /*  305 */
            if (s1.length() > -newPower) {
                /*  306 */
                if (-newPower >= 16)
                    /*  307 */ s1 = s1.substring(0, 16);
                    /*      */
                else {
                    /*  309 */
                    s1 = s1.substring(0, 1 - newPower);
                    /*      */
                }
                /*      */
            }
            /*  312 */
            this.power = newPower;
            /*  313 */
            for (int i = s1.length(); i < 16; ++i)
                /*  314 */
                s1 = s1 + "0";
            /*  315 */
            this.v[0] = Long.parseLong(s1);
            /*  316 */
            s = s.substring(0, loc);
            /*      */
        } else {
            /*  318 */
            this.power = newPower;
            /*  319 */
            this.v[0] = 0L;
            /*      */
        }
        /*      */
        /*  322 */
        int len = s.length();
        /*  323 */
        int sitLoc = 1;
        /*  324 */
        while (len > 0) {
            /*  325 */
            String s1 = "";
            /*  326 */
            if (len > 16) {
                /*  327 */
                s1 = s.substring(len - 16);
                /*  328 */
                s = s.substring(0, len - 16);
                /*      */
            } else {
                /*  330 */
                s1 = s;
                /*  331 */
                s = "";
                /*      */
            }
            /*  333 */
            len = s.length();
            /*  334 */
            this.v[(sitLoc++)] = Long.parseLong(s1);
            /*      */
        }
        /*  336 */
        for (int i = sitLoc; i < this.v.length; ++i)
            /*  337 */
            this.v[i] = 0L;
        /*  338 */
        round(4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(BigDecimal value)
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
        /*  347 */
        if (value.toString().length() <= 16)
            /*  348 */ setValue(value.doubleValue(), value.scale());
            /*      */
        else
            /*  350 */       initByString(value.toString());
        /*      */
    }

    /*      */
    /*      */
    public UFDouble(UFDouble fd)
    /*      */ {
        /*   19 */
        this.power = -8;
        /*      */
        /*  103 */
        this.si = 1;
        /*      */
        /*  106 */
        this.v = new long[5];
        /*      */
        /*  961 */
        this.trimZero = false;
        /*      */
        /*  355 */
        this.si = fd.si;
        /*  356 */
        for (int i = 0; i < this.v.length; ++i) {
            /*  357 */
            this.v[i] = fd.v[i];
            /*      */
        }
        /*  359 */
        this.power = fd.power;
        /*      */
    }

    /*      */
    /*      */
    public UFDouble add(double d1)
    /*      */ {
        /*  366 */
        return add(new UFDouble(d1));
        /*      */
    }

    /*      */
    /*      */
    public UFDouble add(UFDouble ufd)
    /*      */ {
        /*  373 */
        int power = (Math.abs(ufd.getPower()) > Math.abs(getPower())) ? ufd.getPower() : getPower();
        /*      */
        /*  376 */
        return add(ufd, power, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble add(UFDouble ufd, int newPower)
    /*      */ {
        /*  383 */
        return add(ufd, newPower, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble add(UFDouble ufd, int newPower, int roundingMode)
    /*      */ {
        /*  395 */
        newPower = getValidPower(newPower);
        /*      */
        /*  397 */
        UFDouble fdnew = new UFDouble(this);
        /*      */
        /*  399 */
        fdnew.power = newPower;
        /*  400 */
        fdnew.addUp0(ufd, newPower, roundingMode);
        /*  401 */
        return fdnew;
        /*      */
    }

    /*      */
    /*      */
    private void addUp0(double ufd)
    /*      */ {
        /*  408 */
        addUp0(new UFDouble(ufd), this.power, 4);
        /*      */
    }

    /*      */
    /*      */
    private void addUp0(UFDouble ufd, int newPower, int roundingMode)
    /*      */ {
        /*  420 */
        toPlus();
        /*  421 */
        ufd.toPlus();
        /*  422 */
        for (int i = 0; i < this.v.length; ++i) {
            /*  423 */
            this.v[i] += ufd.v[i];
            /*      */
        }
        /*  425 */
        judgNegative();
        /*  426 */
        adjustIncluedFs();
        /*      */
        /*  428 */
        ufd.judgNegative();
        /*  429 */
        round(roundingMode);
        /*      */
    }

    /*      */
    /*      */
    private void adjustIncluedFs()
    /*      */ {
        /*  441 */
        for (int i = 1; i < this.v.length; ++i)
            /*  442 */
            if (this.v[(i - 1)] < 0L) {
                /*  443 */
                this.v[i] -= 1L;
                /*  444 */
                this.v[(i - 1)] += 10000000000000000L;
                /*      */
            } else {
                /*  446 */
                this.v[i] += this.v[(i - 1)] / 10000000000000000L;
                /*  447 */
                this.v[(i - 1)] %= 10000000000000000L;
                /*      */
            }
        /*      */
    }

    /*      */
    /*      */
    private void adjustNotIncluedFs()
    /*      */ {
        /*  453 */
        for (int i = 1; i < this.v.length; ++i) {
            /*  454 */
            this.v[i] += this.v[(i - 1)] / 10000000000000000L;
            /*  455 */
            this.v[(i - 1)] %= 10000000000000000L;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    public int compareTo(Object o) {
        /*  460 */
        return toBigDecimal().compareTo(((UFDouble) o).toBigDecimal());
        /*      */
    }

    /*      */
    /*      */
    private void cutdown() {
        /*  464 */
        int p = -this.power;
        /*  465 */
        this.v[0] = (this.v[0] / POWER_ARRAY[p] * POWER_ARRAY[p]);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble div(double d1) {
        /*  469 */
        UFDouble ufd = new UFDouble(d1);
        /*  470 */
        return div(ufd);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble div(UFDouble ufd) {
        /*  474 */
        return div(ufd, -8);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble div(UFDouble ufd, int power) {
        /*  478 */
        return div(ufd, power, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble div(UFDouble ufd, int power, int roundingMode)
    /*      */ {
        /*  485 */
        int newPower = getValidPower(power);
        /*  486 */
        BigDecimal bd = toBigDecimal();
        /*  487 */
        BigDecimal divisor = ufd.toBigDecimal();
        /*  488 */
        int maxScale = (divisor.scale() > bd.scale()) ? divisor.scale() : bd.scale();
        /*      */
        /*  490 */
        int nPower = Math.abs(power);
        /*  491 */
        maxScale = (maxScale > nPower) ? maxScale : nPower;
        /*      */
        /*  493 */
        ++maxScale;
        /*  494 */
        BigDecimal newbd = bd.divide(divisor, maxScale, RoundingMode.DOWN);
        /*  495 */
        UFDouble ufdNew = new UFDouble(newbd);
        /*  496 */
        return ufdNew.setScale(newPower, roundingMode);
        /*      */
    }

    /*      */
    /*      */
    public double doubleValue()
    /*      */ {
        /*  510 */
        return toDouble().doubleValue();
        /*      */
    }

    /*      */
    /*      */
    public float floatValue() {
        /*  514 */
        return (float) getDouble();
        /*      */
    }

    /*      */
    /*      */
    public double getDouble()
    /*      */ {
        /*  521 */
        return doubleValue();
        /*      */
    }

    /*      */
    /*      */
    public long[] getDV()
    /*      */ {
        /*  530 */
        return this.v;
        /*      */
    }

    /*      */
    /*      */
    public byte getSIValue()
    /*      */ {
        /*  539 */
        return this.si;
        /*      */
    }

    /*      */
    /*      */
    public int intValue()
    /*      */ {
        /*  550 */
        return (int) getDouble();
        /*      */
    }

    /*      */
    /*      */
    private void judgNegative()
    /*      */ {
        /*  563 */
        boolean isFs = false;
        /*  564 */
        for (int i = this.v.length - 1; i >= 0; --i) {
            /*  565 */
            if (this.v[i] < 0L)
                /*      */ {
                /*  567 */
                isFs = true;
                /*  568 */
                break;
                /*      */
            }
            /*  570 */
            if (this.v[i] > 0L)
                /*      */ break;
            /*      */
        }
        /*  573 */
        if (isFs) {
            /*  574 */
            for (int i = 0; i < this.v.length; ++i)
                /*  575 */
                this.v[i] = (-this.v[i]);
            /*  576 */
            this.si = -1;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    public long longValue() {
        /*  581 */
        long d = 0L;
        /*      */
        /*  583 */
        for (int i = this.v.length - 1; i > 0; --i) {
            /*  584 */
            d *= 10000000000000000L;
            /*  585 */
            d += this.v[i];
            /*      */
        }
        /*  587 */
        return (d * this.si);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble multiply(double d1)
    /*      */ {
        /*  592 */
        UFDouble ufD1 = new UFDouble(d1);
        /*  593 */
        return multiply(ufD1, -8, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble multiply(UFDouble ufd) {
        /*  597 */
        return multiply(ufd, -8, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble multiply(UFDouble ufd, int newPower) {
        /*  601 */
        return multiply(ufd, newPower, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble multiply(UFDouble ufd, int newPower, int roundingMode)
    /*      */ {
        /*  614 */
        newPower = getValidPower(newPower);
        /*      */
        /*  616 */
        BigDecimal bd = toBigDecimal();
        /*  617 */
        BigDecimal divisor = ufd.toBigDecimal();
        /*      */
        /*  621 */
        BigDecimal bdn = bd.multiply(divisor);
        /*  622 */
        bdn = bdn.setScale(-newPower, roundingMode);
        /*      */
        /*  624 */
        UFDouble ufdNew = new UFDouble(bdn);
        /*      */
        /*  627 */
        return ufdNew;
        /*      */
    }

    /*      */
    /*      */
    private void round(int roundingMode)
    /*      */ {
        /*  642 */
        boolean increment = true;
        /*  643 */
        switch (roundingMode)
            /*      */ {
            /*      */
            case 0:
                /*  645 */
                increment = true;
                /*  646 */
                break;
            /*      */
            case 2:
                /*  648 */
                increment = this.si == 1;
                /*  649 */
                break;
            /*      */
            case 3:
                /*  651 */
                increment = this.si == -1;
                /*  652 */
                break;
            /*      */
            case 1:
                /*  654 */
                increment = false;
                /*      */
            case 4:
                /*      */
            case 5:
                /*      */
            case 6:
                /*      */
            case 7:
                /*      */
            case 8:
                /*      */
        }
        /*      */
        /*  666 */
        int p = -this.power;
        /*  667 */
        long vxs = POWER_ARRAY[(p + 1)];
        /*      */
        /*  669 */
        if (increment) {
            /*  670 */
            this.v[0] += vxs * 5L;
            /*  671 */
            adjustNotIncluedFs();
            /*      */
        }
        /*  673 */
        cutdown();
        /*      */
        /*  675 */
        boolean isZero = true;
        /*  676 */
        for (int i = 0; i < this.v.length; ++i) {
            /*  677 */
            if (this.v[i] != 0L) {
                /*  678 */
                isZero = false;
                /*  679 */
                break;
                /*      */
            }
            /*      */
        }
        /*  682 */
        if ((this.si == -1) && (isZero))
            /*  683 */ this.si = 1;
        /*      */
    }

    /*      */
    /*      */
    public UFDouble setScale(int power, int roundingMode)
    /*      */ {
        /*  698 */
        UFDouble scaleDouble = null;
        /*  699 */
        if (this.power == power) {
            /*  700 */
            scaleDouble = (UFDouble) clone();
            /*  701 */
            scaleDouble.round(roundingMode);
            /*      */
        } else {
            /*  703 */
            int newPower = getValidPower(power);
            /*  704 */
            BigDecimal bd = toBigDecimal();
            /*  705 */
            bd = bd.setScale(-newPower, roundingMode);
            /*  706 */
            scaleDouble = new UFDouble(bd);
            /*      */
        }
        /*  708 */
        return scaleDouble;
        /*      */
    }

    /*      */
    /*      */
    private void setValue(double d, int newPower)
    /*      */     throws NumberFormatException
    /*      */ {
        /*  718 */
        if (d < 0.0D) {
            /*  719 */
            d = -d;
            /*  720 */
            this.si = -1;
            /*      */
        }
        /*  722 */
        double dd = d;
        /*  723 */
        this.power = getValidPower(newPower);
        /*      */
        /*  726 */
        double dxs = d % 1.0D;
        /*  727 */
        d -= dxs;
        /*  728 */
        double ld = d;
        /*  729 */
        for (int i = 1; i < this.v.length; ++i) {
            /*  730 */
            this.v[i] = (long) (d % 10000000000000000.0D);
            /*  731 */
            d /= 10000000000000000.0D;
            /*      */
        }
        /*  733 */
        long v2 = 0L;
        /*  734 */
        if (dxs == 0.0D) {
            /*  735 */
            v2 = (long) (dxs * 10000000000000000.0D);
            /*      */
        }
        /*  737 */
        else if (dd / ld == 1.0D) {
            /*  738 */
            dxs = 0.0D;
            /*  739 */
            v2 = (long) (dxs * 10000000000000000.0D);
            /*      */
        } else {
            /*  741 */
            if (this.power <= -8) {
                /*  742 */
                int iv = (int) this.v[2];
                /*  743 */
                if (iv != 0) {
                    /*  744 */
                    if (iv >= 1000000)
                        /*  745 */ this.power = 0;
                        /*  746 */
                    else if (iv >= 100000)
                        /*  747 */ this.power = -1;
                        /*  748 */
                    else if (iv >= 10000)
                        /*  749 */ this.power = -2;
                        /*  750 */
                    else if (iv >= 1000)
                        /*  751 */ this.power = -3;
                        /*  752 */
                    else if (iv >= 100)
                        /*  753 */ this.power = -4;
                        /*  754 */
                    else if (iv >= 10)
                        /*  755 */ this.power = -5;
                        /*  756 */
                    else if (iv >= 1)
                        /*  757 */ this.power = -6;
                    /*      */
                } else {
                    /*  759 */
                    iv = (int) this.v[1];
                    /*  760 */
                    if (iv >= 100000000)
                        /*  761 */ this.power = -7;
                    /*      */
                }
                /*  763 */
                if (this.power < 0) {
                    /*  764 */
                    int ii = -this.power;
                    /*      */
                    /*  766 */
                    int i2 = 1;
                    /*      */
                    /*  768 */
                    for (int i = 1; i < ii; ++i) {
                        /*  769 */
                        i2 *= 10;
                        /*  770 */
                        double dxs1 = Math.round(dxs * i2) / i2;
                        /*  771 */
                        double d1 = ld + dxs1;
                        /*  772 */
                        if (dd / d1 == 1.0D) {
                            /*  773 */
                            dxs = dxs1;
                            /*  774 */
                            break;
                            /*      */
                        }
                        /*      */
                    }
                    /*      */
                }
                /*      */
            }
            /*  779 */
            v2 = (long) ((dxs + 9.999999999999999E-012D) * 10000000000000000.0D);
            /*      */
        }
        /*      */
        /*  782 */
        this.v[0] = v2;
        /*  783 */
        round(4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble sub(double d1) {
        /*  787 */
        UFDouble ufd = new UFDouble(d1);
        /*  788 */
        return sub(ufd, -8, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble sub(UFDouble ufd) {
        /*  792 */
        int power = (Math.abs(ufd.getPower()) > Math.abs(getPower())) ? ufd.getPower() : getPower();
        /*      */
        /*  794 */
        return sub(ufd, power, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble sub(UFDouble ufd, int newPower) {
        /*  798 */
        return sub(ufd, newPower, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble sub(UFDouble ufd, int newPower, int roundingMode)
    /*      */ {
        /*  812 */
        newPower = getValidPower(newPower);
        /*      */
        /*  814 */
        UFDouble ufdnew = new UFDouble(ufd);
        /*  815 */
        ufdnew.si = (byte) (-ufdnew.si);
        /*  816 */
        return add(ufdnew, newPower, roundingMode);
        /*      */
    }

    /*      */
    /*      */
    public static UFDouble sum(double[] dArray)
    /*      */ {
        /*  826 */
        return sum(dArray, -8);
        /*      */
    }

    /*      */
    /*      */
    public static UFDouble sum(double[] dArray, int newPower)
    /*      */ {
        /*  838 */
        newPower = getValidPower(newPower);
        /*      */
        /*  840 */
        UFDouble ufd = new UFDouble(0, newPower);
        /*  841 */
        for (int i = 0; i < dArray.length; ++i) {
            /*  842 */
            ufd.addUp0(dArray[i]);
            /*      */
        }
        /*  844 */
        return ufd;
        /*      */
    }

    /*      */
    /*      */
    public static UFDouble sum(double[] dArray, int newPower, int roundingMode)
    /*      */ {
        /*  857 */
        newPower = getValidPower(newPower);
        /*      */
        /*  859 */
        UFDouble ufd = new UFDouble(0, newPower);
        /*  860 */
        for (int i = 0; i < dArray.length; ++i) {
            /*  861 */
            UFDouble ufdNew = new UFDouble(dArray[i], newPower);
            /*  862 */
            ufd.addUp0(ufdNew, newPower, roundingMode);
            /*      */
        }
        /*  864 */
        return ufd;
        /*      */
    }

    /*      */
    /*      */
    public BigDecimal toBigDecimal()
    /*      */ {
        /*  875 */
        return new BigDecimal(toString());
        /*      */
    }

    /*      */
    /*      */
    public BigDecimal toBigDecimal(int precious, RoundingMode mode) {
        /*  879 */
        return new BigDecimal(toString(), new MathContext(precious, mode));
        /*      */
    }

    /*      */
    /*      */
    public Double toDouble()
    /*      */ {
        /*  888 */
        return new Double(toString());
        /*      */
    }

    /*      */
    /*      */
    private void toPlus()
    /*      */ {
        /*  900 */
        if (this.si == 1)
            /*  901 */ return;
        /*  902 */
        this.si = 1;
        /*  903 */
        for (int i = 0; i < this.v.length; ++i)
            /*  904 */
            this.v[i] = (-this.v[i]);
        /*      */
    }

    /*      */
    /*      */
    public String toString()
    /*      */ {
        /*  910 */
        boolean addZero = false;
        /*  911 */
        StringBuffer sb = new StringBuffer();
        /*  912 */
        if (this.si == -1)
            /*  913 */ sb.append("-");
        /*  914 */
        for (int i = this.v.length - 1; i > 0; --i) {
            /*  915 */
            if ((this.v[i] == 0L) && (!(addZero)))
                /*      */ continue;
            /*  917 */
            String temp = String.valueOf(this.v[i]);
            /*  918 */
            if (addZero) {
                /*  919 */
                int len = temp.length();
                /*  920 */
                int addZeroNo = 16 - len;
                /*  921 */
                for (int j = 0; j < addZeroNo; ++j) {
                    /*  922 */
                    sb.append('0');
                    /*      */
                }
                /*      */
            }
            /*  925 */
            sb.append(temp);
            /*  926 */
            addZero = true;
            /*      */
        }
        /*  928 */
        if (!(addZero)) {
            /*  929 */
            sb.append('0');
            /*      */
        }
        /*  931 */
        if (this.power < 0) {
            /*  932 */
            sb.append('.');
            /*  933 */
            for (int j = 0; (j < 16) && (j < -this.power); ++j) {
                /*  934 */
                sb.append(this.v[0] / POWER_ARRAY[(j + 1)] % 10L);
                /*      */
            }
            /*      */
        }
        /*      */
        /*  938 */
        int index = -1;
        /*  939 */
        if ((isTrimZero()) &&
                /*  940 */       (this.power < 0)) {
            /*  941 */
            String sTemp = sb.toString();
            /*  942 */
            for (int i = sb.length() - 1; i >= 0; --i) {
                /*  943 */
                if (sTemp.substring(i, i + 1).equals("0")) {
                    /*  944 */
                    index = i;
                    /*      */
                } else {
                    /*  946 */
                    if (!(sTemp.substring(i, i + 1).equals("."))) break;
                    /*  947 */
                    index = i;
                    break;
                    /*      */
                }
                /*      */
                /*      */
            }
            /*      */
            /*      */
        }
        /*      */
        /*  954 */
        if (index >= 0)
            /*  955 */ sb = sb.delete(index, sb.length());
        /*  956 */
        return sb.toString();
        /*      */
    }

    /*      */
    /*      */
    public UFDouble abs()
    /*      */ {
        /*  969 */
        UFDouble fdnew = new UFDouble();
        /*  970 */
        fdnew.power = this.power;
        /*  971 */
        fdnew.si = 1;
        /*  972 */
        for (int i = 0; i < this.v.length; ++i) {
            /*  973 */
            fdnew.v[i] = this.v[i];
            /*      */
        }
        /*  975 */
        return fdnew;
        /*      */
    }

    /*      */
    /*      */
    public int getPower()
    /*      */ {
        /*  984 */
        return this.power;
        /*      */
    }

    /*      */
    /*      */
    public boolean isTrimZero()
    /*      */ {
        /*  993 */
        return this.trimZero;
        /*      */
    }

    /*      */
    /*      */
    public UFDouble mod(UFDouble ufd)
    /*      */ {
        /* 1003 */
        return mod(ufd, -8, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble mod(UFDouble ufd, int newPower)
    /*      */ {
        /* 1013 */
        return mod(ufd, newPower, 4);
        /*      */
    }

    /*      */
    /*      */
    public UFDouble mod(UFDouble ufd, int newPower, int roundingMode)
    /*      */ {
        /* 1023 */
        UFDouble ufdDiv = div(ufd, 0, 1);
        /* 1024 */
        UFDouble ufdnew = sub(ufdDiv.multiply(ufd));
        /* 1025 */
        if (ufd.si != this.si)
            /* 1026 */ ufdnew = ufdnew.sub(ufd);
        /* 1027 */
        if (ufdnew.si != this.si)
            /* 1028 */ ufdnew = ufdnew.sub(ufd);
        /* 1029 */
        ufdnew.power = newPower;
        /* 1030 */
        ufdnew.round(roundingMode);
        /* 1031 */
        return ufdnew;
        /*      */
    }

    /*      */
    /*      */
    public void setTrimZero(boolean newTrimZero)
    /*      */ {
        /* 1041 */
        this.trimZero = newTrimZero;
        /*      */
    }

    /*      */
    /*      */
    private static int getValidPower(int newPower)
    /*      */ {
        /* 1046 */
        int power = (newPower > 0) ? -newPower : newPower;
        /* 1047 */
        if (power < -16)
            /* 1048 */ power = -16;
        /* 1049 */
        return power;
        /*      */
    }

    /*      */
    /*      */
    public int hashCode()
    /*      */ {
        /* 1055 */
        int v = 0;
        /* 1056 */
        for (int i = 0; i < this.v.length; ++i) {
            /* 1057 */
            v = (int) (v + this.v[i]);
            /*      */
        }
        /* 1059 */
        return (v * this.si);
        /*      */
    }

    /*      */
    /*      */
    public boolean equals(Object o)
    /*      */ {
        /* 1064 */
        if (o instanceof UFDouble) {
            /* 1065 */
            UFDouble ud = (UFDouble) o;
            /* 1066 */
            return ((this.si == ud.si) && (Arrays.equals(this.v, ud.v)));
            /*      */
        }
        /*      */
        /* 1069 */
        return false;
        /*      */
    }

    /*      */
    /*      */
    public Object clone() {
        /* 1073 */
        return new UFDouble(this);
        /*      */
    }

    /*      */
    /*      */   static
        /*      */ {
        /*  109 */
        for (int i = 0; i < POWER_ARRAY.length - 1; ++i) {
            /*  110 */
            POWER_ARRAY[i] = (long) Math.pow(10.0D, 16 - i);
            /*      */
        }
        /*  112 */
        POWER_ARRAY[(POWER_ARRAY.length - 1)] = 0L;
        /*      */
        /*  115 */
        ONE_DBL = new UFDouble(1.0D);
        /*      */
        /*  117 */
        ZERO_DBL = new UFDouble(0.0D);
        /*      */
    }
    /*      */
}

/* Location:           D:\home\NCCHOME9034_EPA_green\NCCHOME9034_EPA\external\lib\basic.jar
 * Qualified Name:     nc.vo.pub.lang.UFDouble
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.5.3
 */