Simple-Workflow-Engine
======================

Simple workflow engine for basic ETL processing

The basic idea is that this provides a workflow that will be executed on
command.  The workflow consists of one or more actors, and each actor can
act as a provider (provides data to upstream components) and/or a consumer
(receives data from downstream components).

Actors can be extended to perform necessary tasks (e.g. pull data from a
database, upload videos to Brightcove Video Cloud, etc).

All control and wiring is managed via XML files.  Each actor is defined
by an <actor> element.  Each <actor> element can have one or more
provider actor, and one or more consumer actor.

Execution begins by finding all "terminal" actors - i.e. actors that have no
consumers.  These are presumed to be the end actors in the workflow.  The
engine calls run() on these elements, who in turn call run() on their
providers (and on down).  Each provider, as it has data to pass on in the
workflow, will pass data as Record objects to each of its consumers.

When a consumer receives a null Record from its provider, it is assumed that
provider is now complete.  When all actors are complete, the engine will send
a finalize() to each actor to perform any necessary cleanup.