import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import marksCSS from './marks.module.css';
import {marks, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {setActNew} from "../AnalyticsMain";
import ErrFound from "../../other/error/ErrFound";
import {eventSource, sendToServer} from "../../main/Main";
import {CHANGE_EVENTS_CLEAR, changeEvents, changeMarks} from "../../../store/actions";

let dispatch, marksInfo, maxEl, errText, cState, selKid;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
maxEl = 0;

function onCon(e) {
    setInfo();
}

function setInfo() {
    sendToServer({
        uuid: cState.uuid
    }, 'POST', "journal/getInfoPers")
        .then(data => {
            console.log(data);
            if (data.error == false) {
                if(cState.role == 1 && cState.kid) selKid = cState.kid;
                dispatch(changeMarks("namePers", data.bodyPers));
                dispatch(changeMarks("jur", data.bodyM));
            }
        });
}

export function Marks() {
    marksInfo = useSelector(marks);
    cState = useSelector(states);
    let jur, pers;
    if(marksInfo.namePers) {
        pers = Object.getOwnPropertyNames(marksInfo.namePers);
    }
    if(marksInfo.jur) {
        jur = Object.getOwnPropertyNames(marksInfo.jur);
    }
    maxEl = pers.length;
    if(!dispatch) setActNew(4);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Marks.jsx");
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        let scr = document.querySelector("." + marksCSS.predm);
        if(scr) scr.scrollTo(scr.scrollWidth, 0);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            console.log("I was triggered during componentWillUnmount Marks.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(cState.role == 1 && cState.kid && selKid != cState.kid) {
            selKid = cState.kid;
            setInfo();
        }
        console.log('componentDidUpdate Marks.jsx');
    });
    return <div className={marksCSS.AppHeader}>
        <Helmet>
            <title>Итоговые оценки</title>
        </Helmet>
        {!marksInfo.namePers || !maxEl ?
                <ErrFound text={errText}/>
            :
                <div className={marksCSS.blockPredm}>
                    <div className={marksCSS.predm}>
                        <div className={marksCSS.persGrid}>
                            <div className={marksCSS.nav_i} id={marksCSS.nav_i}>
                                <br/>
                            </div>
                            {marksInfo.namePers && pers.map(param =>
                                <div className={marksCSS.nav_i}>
                                    <div className={marksCSS.nav_iTextPer} data-s={pers.length > 2 ? 1 : 0}>
                                        {marksInfo.namePers[param]}
                                    </div>
                                </div>
                            )}
                        </div>
                        {jur && jur.map(param =>
                            <div className={marksCSS.predmGrid} id={param}>
                                <div className={marksCSS.nav_i+" nam " + marksCSS.nam} id={marksCSS.nav_i}>
                                    {param}
                                </div>
                                <div className={marksCSS.nav_i+" "+marksCSS.nav_iBr} id={marksCSS.nav_i}>
                                    <br/>
                                </div>
                                {marksInfo.namePers && pers.map(param1 =>
                                    <div className={marksCSS.nav_i} id={marksCSS.nav_i}>
                                        {marksInfo.jur ? marksInfo.jur[param][param1] : <br/>}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
        }
    </div>
}
export default Marks;