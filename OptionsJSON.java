package com.baddog.optionsscanner;

import java.util.List;

/**
 * Created by Brian on 2018-02-17.
 */

public class OptionsJSON {
    List<OptionExpiryDateJSON> optionChain;

    public OptionExpiryDateJSON getOptionExpiryDateJSON(int index) {return this.optionChain.get(index);}
    public int getOptionExpiryDateJSONSize() {return this.optionChain.size();}

    public static class OptionExpiryDateJSON {

        String expiryDate;
        String description;
        String listingExchange;
        String optionExerciseType;
        List<ChainPerRootJSON> chainPerRoot;

        public  ChainPerRootJSON getChainPerRoot(int index) {
            return this.chainPerRoot.get(index); }
        public  int getStrikePriceChainCount() {
            return this.chainPerRoot.size();
        }
    }

    public static class ChainPerRootJSON {
        String optionRoot;
        List<StrikePriceJSON> chainPerStrikePrice;
        int multiplier;

        public StrikePriceJSON getStrikePrice (int index) {
            return this.chainPerStrikePrice.get(index);
        }
        int getStrikePricesCount() {
            return this.chainPerStrikePrice.size();
        }

    }

    public static class StrikePriceJSON {
        double strikePrice;
        int callSymbolId;
        int putSymbolId;
    }
}

