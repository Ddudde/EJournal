import React, {useEffect, useRef} from "react";
import eventsCSS from './events.module.css';
import {useDispatch, useSelector} from "react-redux";
import {events} from "../../../store/selector";
import warn from '../../../media/warning.png';
import {CHANGE_EVENT_DEL, CHANGE_EVENT_TIMER, CHANGE_EVENTS_RL, changeEvents} from "../../../store/actions";
import no from "../../../media/no.png";

let dispatch, eventsInfo, evHeader;

function f(e) {
    dispatch(changeEvents(CHANGE_EVENTS_RL, !eventsInfo.right));
}

function onDel(e) {
    let par, id;
    par = e.target;
    if (par.hasAttribute("data-id")) {
        id = par.getAttribute("data-id");
        dispatch(changeEvents(CHANGE_EVENT_DEL, undefined, id));
    }
}

function delT(id) {
    dispatch(changeEvents(CHANGE_EVENT_DEL, true, id));
}

export function Events() {
    eventsInfo = useSelector(events);
    // if(!dispatch) setActNew(0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Events.jsx");
        for(let id of Object.getOwnPropertyNames(eventsInfo.time)){
            if(eventsInfo.time[id].init) continue;
            setTimeout(()=>(delT(id)),eventsInfo.time[id].long*1000);
            dispatch(changeEvents(CHANGE_EVENT_TIMER, true, id));
        }
        return function() {
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount Events.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        for(let id of Object.getOwnPropertyNames(eventsInfo.time)){
            if(eventsInfo.time[id].init) continue;
            setTimeout(()=>(delT(id)),eventsInfo.time[id].long*1000);
            dispatch(changeEvents(CHANGE_EVENT_TIMER, true, id));
        }
        console.log('componentDidUpdate Events.jsx');
    });
    return (
        <div className={eventsCSS.evHeader} style={{top: (7*eventsInfo.steps) + "vh", left: eventsInfo.right ? "" : "1vw", right: eventsInfo.right ? "1vw" : ""}} ref={(el)=>(evHeader = el)}>
            {Object.getOwnPropertyNames(eventsInfo.evs).reverse().map(param =>
                <div className={eventsCSS.warne} key={param}>
                    <img src={warn} className={eventsCSS.warnimg} alt=""/>
                    <span className={eventsCSS.title}>
                        {eventsInfo.evs[param].title}
                    </span>
                    <img className={eventsCSS.imgCl} data-id={param} src={no} onClick={onDel} title="Удалить" alt=""/>
                    <div className={eventsCSS.text}>
                        {eventsInfo.evs[param].text}
                    </div>
                    <div className={eventsCSS.time}>
                        {eventsInfo.evs[param].dtime}
                    </div>
                </div>
            )}
        </div>
    )
}
export default Events;