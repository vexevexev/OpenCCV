-module(open_ccv).
-behaviour(application).

-export([start/2, stop/1]).

start(_Type, _Args) ->
    open_ccv_supervisor:start_link().

stop(_State) ->
    ok.