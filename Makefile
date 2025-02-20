JAVAC=/usr/bin/javac
.SUFFIXES: .java .class
SRCDIR=src/barScheduling
BINDIR=bin
$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES= DrinkOrder.class Barman.class Patron.class SchedulingSimulation.class
CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)
clean:
	rm $(BINDIR)/barScheduling/*.class
FCFS: $(CLASS_FILES)
	java -cp bin barScheduling/SchedulingSimulation 100 0
SJF: $(CLASS_FILES)
	java -cp bin barScheduling/SchedulingSimulation 100 1

