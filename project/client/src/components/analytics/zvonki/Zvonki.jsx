import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import analyticsCSS from '../analyticsMain.module.css';
import zvonkiCSS from './zvonki.module.css';
import {states, zvonki} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {chStatB, ele, onClose, onDel, onEdit, onFin, setActNew} from "../AnalyticsMain";
import ErrFound from "../../other/error/ErrFound";
import yes from "../../../media/yes.png";
import {
    CHANGE_ZVONKI,
    CHANGE_ZVONKI_DEL,
    CHANGE_ZVONKI_DEL_L0,
    CHANGE_ZVONKI_L1,
    CHANGE_ZVONKI_SMENA
} from "../../../store/actions";
import no from "../../../media/no.png";
import ed from "../../../media/edit.png";

let dispatch, zvonkiInfo, errText, inps, cState;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
inps = {inpnpt : "8.00-8.45", inpnst: "1 смена"};
let [_, forceUpdate] = [];

function getZvonki(b) {
    return b ?
            <>
                {Object.getOwnPropertyNames(zvonkiInfo).map(param =>
                    <div className={zvonkiCSS.smenaGrid} key={param}>
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            №
                        </div>
                        <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} style={{gridColumn: "2"}} data-st="0">
                            <div className={analyticsCSS.fi}>
                                <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                    {zvonkiInfo[param].name}
                                </div>
                                <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onDel(e, CHANGE_ZVONKI_DEL_L0)} title="Удалить" alt=""/>
                            </div>
                            <div className={analyticsCSS.ed}>
                                <div className={analyticsCSS.preinf}>
                                    Смена:
                                </div>
                                <input className={analyticsCSS.inp} data-id1={param} id={"inpnst_" + param} placeholder={"1 смена"} defaultValue={zvonkiInfo[param].name} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                {ele(false, "inpnst_" + param, inps)}
                                <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_ZVONKI_L1)} title="Подтвердить" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                            </div>
                        </div>
                        {zvonkiInfo[param].lessons && Object.getOwnPropertyNames(zvonkiInfo[param].lessons).map((param1, i) =>
                            <>
                                <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                                    {i + 1}
                                </div>
                                <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} key={param1} data-st="0">
                                    <div className={analyticsCSS.fi}>
                                        <div className={analyticsCSS.nav_i+" "+analyticsCSS.nav_iZag2} id={analyticsCSS.nav_i}>
                                            {zvonkiInfo[param].lessons[param1]}
                                        </div>
                                        <img className={analyticsCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onDel(e, CHANGE_ZVONKI_DEL)} title="Удалить" alt=""/>
                                    </div>
                                    <div className={analyticsCSS.ed}>
                                        <div className={analyticsCSS.preinf}>
                                            Интервал:
                                        </div>
                                        <input className={analyticsCSS.inp} data-id={param + "_" + param1} id={"inpnpt_" + param + "_" + param1} placeholder={"8.00-8.45"} defaultValue={zvonkiInfo[param].lessons[param1]} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                        {ele(false, "inpnpt_" + param + "_" + param1, inps)}
                                        <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_ZVONKI)} title="Подтвердить" alt=""/>
                                        <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                    </div>
                                </div>
                            </>
                        )}
                        <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                            X
                        </div>
                        <div className={analyticsCSS.add} data-st="0">
                            <div className={analyticsCSS.nav_i+" "+analyticsCSS.link} id={analyticsCSS.nav_i} onClick={onEdit}>
                                Добавить интервал
                            </div>
                            <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                                <div className={analyticsCSS.preinf}>
                                    Интервал:
                                </div>
                                <input className={analyticsCSS.inp} data-id1={param} id={"inpnpt_"} placeholder={"8.00-8.45"} defaultValue={inps.inpnpt} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                {ele(false, "inpnpt_", inps)}
                                <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_ZVONKI, zvonkiInfo)} title="Подтвердить" alt=""/>
                                <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                            </div>
                        </div>
                    </div>
                )}
                <div className={zvonkiCSS.smenaGrid}>
                    <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                        X
                    </div>
                    <div className={analyticsCSS.add} data-st="0" style={{gridColumn: "2"}}>
                        <div className={analyticsCSS.nav_i+" "+analyticsCSS.link} id={analyticsCSS.nav_i} onClick={onEdit}>
                            Добавить смену
                        </div>
                        <div className={analyticsCSS.edbl+" "+analyticsCSS.nav_iZag3} data-st="0">
                            <div className={analyticsCSS.preinf}>
                                Смена:
                            </div>
                            <input className={analyticsCSS.inp} id={"inpnst_"} placeholder={"X Смена"} defaultValue={inps.inpnst} onChange={(e)=>chStatB(e, inps)} type="text"/>
                            {ele(false, "inpnst_", inps)}
                            <img className={analyticsCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_ZVONKI_SMENA, zvonkiInfo)} title="Подтвердить" alt=""/>
                            <img className={analyticsCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                        </div>
                    </div>
                </div>
            </>
        :
            Object.getOwnPropertyNames(zvonkiInfo).map(param =>
                <div className={zvonkiCSS.smenaGrid} key={param}>
                    <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                        №
                    </div>
                    <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i} style={{gridColumn: "2"}}>
                        {zvonkiInfo[param].name}
                    </div>
                    {Object.getOwnPropertyNames(zvonkiInfo[param].lessons).map((param1, i) =>
                        <>
                            <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                                {i + 1}
                            </div>
                            <div className={analyticsCSS.nav_i} id={analyticsCSS.nav_i}>
                                {zvonkiInfo[param].lessons[param1]}
                            </div>
                        </>
                    )}
                </div>
            )
}

export function Zvonki() {
    zvonkiInfo = useSelector(zvonki);
    cState = useSelector(states);
    if(!dispatch) setActNew(0);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Zvonki.jsx");
        for(let el of document.querySelectorAll("." + analyticsCSS.edbl + " *[id^='inpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount Zvonki.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Zvonki.jsx');
    });
    return (
        <div className={analyticsCSS.header}>
            <Helmet>
                <title>Расписание звонков</title>
            </Helmet>
            {Object.getOwnPropertyNames(zvonkiInfo).length == 0 && cState.role != 3 ?
                    <ErrFound text={errText}/>
                :
                    <div className={analyticsCSS.block}>
                        <div className={analyticsCSS.l1}>
                            {getZvonki(cState.role == 3)}
                        </div>
                    </div>
            }
        </div>
    )
}
export default Zvonki;