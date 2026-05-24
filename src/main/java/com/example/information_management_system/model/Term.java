package com.example.information_management_system.model;

public class Term {
    private String term;
    private boolean open;
    private boolean current;

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
}
