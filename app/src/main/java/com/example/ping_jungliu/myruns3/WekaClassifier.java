package com.example.ping_jungliu.myruns3;

/**
 * Created by Ping-Jung Liu on 2018/2/10.
 */

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N31edd7620(i);
        return p;
    }
    static double N31edd7620(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 320.825104) {
            p = WekaClassifier.N5b3cc571(i);
        } else if (((Double) i[0]).doubleValue() > 320.825104) {
            p = 2;
        }
        return p;
    }
    static double N5b3cc571(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 45.216882) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 45.216882) {
            p = WekaClassifier.Nf2d1db2(i);
        }
        return p;
    }
    static double Nf2d1db2(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 260.664075) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 260.664075) {
            p = WekaClassifier.N6791ef6c3(i);
        }
        return p;
    }
    static double N6791ef6c3(Object []i) {
        double p = Double.NaN;
        if (i[9] == null) {
            p = 1;
        } else if (((Double) i[9]).doubleValue() <= 3.673385) {
            p = 1;
        } else if (((Double) i[9]).doubleValue() > 3.673385) {
            p = WekaClassifier.N3818623b4(i);
        }
        return p;
    }
    static double N3818623b4(Object []i) {
        double p = Double.NaN;
        if (i[5] == null) {
            p = 2;
        } else if (((Double) i[5]).doubleValue() <= 13.221909) {
            p = 2;
        } else if (((Double) i[5]).doubleValue() > 13.221909) {
            p = WekaClassifier.N23d2c01b5(i);
        }
        return p;
    }
    static double N23d2c01b5(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 296.095641) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 296.095641) {
            p = 2;
        }
        return p;
    }
}