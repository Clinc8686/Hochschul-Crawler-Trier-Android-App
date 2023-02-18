package de.clinc8686.hochschul_crawler;

public class LoginSuccess {
    public boolean initialised = false;
    private onValueChangeListener valueChangeListener;

    LoginSuccess(boolean value) {
        this.initialised = value;
    }

    public boolean getVariable() {
        return initialised;
    }

    public void setVariable(boolean value) {
        initialised = value;
        if (valueChangeListener != null) valueChangeListener.onChange();
    }

    public onValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(onValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    public interface onValueChangeListener {
        void onChange();
    }
}
