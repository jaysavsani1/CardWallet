package com.quixom.cardwallet.internal;

import android.widget.EditText;

import com.quixom.cardwallet.fields.CreditEntryFieldBase;
import com.quixom.cardwallet.library.CardType;


/**
 * contract for delegate
 *
 * TODO gut this delegate business
 */
public interface CreditCardFieldDelegate {
  void onCardTypeChange(CardType type);

  void onCreditCardNumberValid(String remainder);

  void onBadInput(EditText field);

  void focusOnField(CreditEntryFieldBase field, String initialValue);

  void focusOnPreviousField(CreditEntryFieldBase field);
}
