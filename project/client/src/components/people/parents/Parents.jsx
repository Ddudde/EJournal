import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import peopleCSS from '../peopleMain.module.css';
import parentsCSS from './parents.module.css';
import {classmates, groups, parents, states, themes} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {
    chStatB,
    copyLink,
    ele,
    goToProf,
    onClose,
    onDel,
    onEdit,
    onFin,
    refreshLink,
    setActNew, setEvGr,
    sit
} from "../PeopleMain";
import profl from "../../../media/profl.png";
import profd from "../../../media/profd.png";
import Pane from "../../other/pane/Pane";
import ErrFound from "../../other/error/ErrFound";
import mapd from "../../../media/Map_symbolD.png";
import mapl from "../../../media/Map_symbolL.png";
import {
    CHANGE_CLASSMATES_GL,
    CHANGE_EVENT,
    CHANGE_EVENTS_CLEAR, CHANGE_GROUPS_GL, CHANGE_GROUPS_GR,
    CHANGE_PARENTS,
    CHANGE_PARENTS_DEL,
    CHANGE_PARENTS_DEL_L0,
    CHANGE_PARENTS_DEL_L1,
    CHANGE_PARENTS_GL,
    CHANGE_PARENTS_L1,
    CHANGE_PARENTS_L1_PARAM, CHANGE_SCHEDULE_GL, CHANGE_TEACHERS_GL, changeAnalytics,
    changeEvents, changeGroups,
    changePeople
} from "../../../store/actions";
import ed from "../../../media/edit.png";
import yes from "../../../media/yes.png";
import no from "../../../media/no.png";
import refreshCd from "../../../media/refreshCd.png";
import refreshCl from "../../../media/refreshCl.png";
import copyd from "../../../media/copyd.png";
import copyl from "../../../media/copyl.png";
import {eventSource, sendToServer} from "../../main/Main";
import {cParents, cAuth} from "../../other/Controllers";

let dispatch, parentsInfo, groupsInfo, selGr, classmatesInfo, errText, inps, themeState, cState;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
inps = {nyid : undefined, inpnpt : "Фамилия И.О."};
selGr = 0;
let [_, forceUpdate] = [];

function selecKid(e, id) {
    inps.nyid = id;
    dispatch(changePeople(CHANGE_PARENTS_L1_PARAM, undefined, "nw", undefined, classmatesInfo[inps.nyid].name));
}

function getKids() {
    inps.ppI = inps.nyid && parentsInfo.nw.par ? Object.getOwnPropertyNames(parentsInfo.nw.par) : [];
    return (inps.nyid &&
        <div className={parentsCSS.blockList}>
            <div className={peopleCSS.nav_i+' '+parentsCSS.selEl} id={peopleCSS.nav_i}>
                <div className={parentsCSS.elInf}>Ученик:</div>
                <div className={parentsCSS.elText}>{parentsInfo.nw.name}</div>
                <img className={parentsCSS.mapImg} data-enablem={inps.lpI.length < 2 ? "0" : "1"} src={themeState.theme_ch ? mapd : mapl} alt=""/>
            </div>
            <div className={parentsCSS.list}>
                {parentsInfo && inps.lpI.map(param1 =>
                    param1 != inps.nyid &&
                    <div className={peopleCSS.nav_i+' '+parentsCSS.listEl} key={param1} id={peopleCSS.nav_i} onClick={e => (selecKid(e, param1))}>
                        <div className={parentsCSS.elInf}>Ученик:</div>
                        <div className={parentsCSS.elText}>{classmatesInfo[param1].name}</div>
                    </div>
                )}
            </div>
        </div>
    )
}

function getAddPred(param) {
    return(
        <div className={peopleCSS.add+" "+peopleCSS.nav_iZag} data-st="0">
            <div className={peopleCSS.nav_i+" "+peopleCSS.link} id={peopleCSS.nav_i} onClick={onEdit}>
                Добавить представителя
            </div>
            <div className={peopleCSS.pepl} data-st="0">
                <div className={peopleCSS.fi}>
                    <div className={peopleCSS.nav_i + " " + peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                        {inps.inpnpt}
                    </div>
                    <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                    <img className={peopleCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, inps, forceUpdate, CHANGE_PARENTS, parentsInfo)} title="Подтвердить" alt=""/>
                    <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Выйти из режима создания" alt=""/>
                </div>
                <div className={peopleCSS.ed}>
                    <div className={peopleCSS.preinf}>
                        ФИО:
                    </div>
                    <input className={peopleCSS.inp} data-id1={param ? param : undefined} id={"inpnpt_"} placeholder={"Фамилия И.О."} defaultValue={inps.inpnpt} onChange={e=>chStatB(e, inps)} type="text"/>
                    {ele(false, "inpnpt_", inps)}
                    <img className={peopleCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, inps, forceUpdate)} title="Подтвердить" alt=""/>
                    <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                </div>
            </div>
        </div>
    )
}

function getAdd() {
    return (
        <div className={peopleCSS.add+" "+peopleCSS.nav_iZag} data-st="0">
            <div className={peopleCSS.nav_i+" "+peopleCSS.link} data-enable={inps.nyid ? 1 : 0} id={peopleCSS.nav_i} onClick={onEdit}>
                Добавить ученику представителей
            </div>
            <div className={peopleCSS.pepl} style={{marginBlock: "unset"}}>
                <div className={parentsCSS.uch}>
                    <div className={peopleCSS.nav_i+" "+parentsCSS.nam} id={peopleCSS.nav_i}>
                        Обучающийся:
                    </div>
                    {getKids()}
                    <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                        {inps.ppI.length > 1 ? "Представители:" : "Представитель:"}
                    </div>
                </div>
                {getAddPred()}
                {inps.ppI.map((param1, i, xs, info = parentsInfo.nw.par[param1]) =>
                    <div className={peopleCSS.pepl} data-st="0" key={param1}>
                        <div className={peopleCSS.fi}>
                            <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                {info.name}
                            </div>
                            <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                            <img className={peopleCSS.imginp} data-id={param1} style={{marginRight: "1vw"}} src={no} onClick={e=>onDel(e, CHANGE_PARENTS_DEL)} title="Удалить" alt=""/>
                        </div>
                        <div className={peopleCSS.ed}>
                            <div className={peopleCSS.preinf}>
                                ФИО:
                            </div>
                            <input className={peopleCSS.inp} data-id1={param1} id={"inpnpt_" + param1} placeholder={"Фамилия И.О."} defaultValue={info.name} onChange={e=>chStatB(e, inps)} type="text"/>
                            {ele(false, "inpnpt_" + param1, inps)}
                            <img className={peopleCSS.imginp+" yes "} src={yes} onClick={e=>onFin(e, inps, forceUpdate, CHANGE_PARENTS, parentsInfo)} title="Подтвердить" alt=""/>
                            <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                        </div>
                    </div>
                )}
                <div className={parentsCSS.upr}>
                    <img className={peopleCSS.imginp+" yes "} src={yes} data-enable={(parentsInfo.nw && parentsInfo.nw.par && Object.getOwnPropertyNames(parentsInfo.nw.par).length > 0) ? "1" : "0"} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_PARENTS, parentsInfo)} title="Подтвердить" alt=""/>
                    <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onClose(e, true)} title="Выйти из режима создания" alt=""/>
                </div>
            </div>
        </div>
    );
}

function getParents (pI, b) {
    return b ?
            <>
                {getAdd()}
                {pI.map((param, i, xs, info = parentsInfo[param], ppI = info.par ? Object.getOwnPropertyNames(info.par) : [])=>
                    (param != "nw" && info.par) &&
                    <div className={peopleCSS.nav_iZag+" "+peopleCSS.nav_iZag1} key={param}>
                        <div className={parentsCSS.uch}>
                            <div className={peopleCSS.nav_i+" "+parentsCSS.nam} id={peopleCSS.nav_i}>
                                Обучающийся: {info.name}
                            </div>
                            {info.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info.login)} title="Перейти в профиль" alt=""/>}
                            <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                                {ppI.length > 1 ? "Представители:" : "Представитель:"}
                            </div>
                        </div>
                        {getAddPred(param)}
                        {ppI.map((param1, i1, xs1, info1 = info.par[param1], codeLink = info1 && info1.link ? sit + (info1.login ? "/reauth/" : "/invite/") + info1.link : undefined) =>
                            <div className={peopleCSS.pepl} data-st="0" key={param1}>
                                <div className={peopleCSS.fi}>
                                    <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                        {info1.name}
                                    </div>
                                    {info1.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info1.login)} title="Перейти в профиль" alt=""/>}
                                    <img className={peopleCSS.imgfield} src={ed} onClick={onEdit} title="Редактировать" alt=""/>
                                    <img className={peopleCSS.imginp} data-id={param + "_" + param1} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onDel(e, CHANGE_PARENTS_DEL, parentsInfo)} title="Удалить" alt=""/>
                                    <input className={peopleCSS.inp+" "+peopleCSS.copyInp} data-id={param + "_" + param1} id={"inpcpt_" + param + "_" + param1} placeholder="Ссылка не создана" defaultValue={codeLink} type="text" readOnly/>
                                    <img className={peopleCSS.imginp+" "+peopleCSS.refrC} src={themeState.theme_ch ? refreshCd : refreshCl} onClick={(e)=>refreshLink(e, sit, CHANGE_PARENTS)} title="Создать ссылку-приглашение" alt=""/>
                                    <img className={peopleCSS.imginp} src={themeState.theme_ch ? copyd : copyl} title="Копировать" data-enable={info1.link ? "1" : "0"} onClick={(e)=>copyLink(e, info1.link, info1.name)} alt=""/>
                                </div>
                                <div className={peopleCSS.ed}>
                                    <div className={peopleCSS.preinf}>
                                        ФИО:
                                    </div>
                                    <input className={peopleCSS.inp} data-id={param + "_" + param1} id={"inpnpt_" + param1} placeholder={"Фамилия И.О."} defaultValue={info1.name} onChange={(e)=>chStatB(e, inps)} type="text"/>
                                    {ele(false, "inpnpt_" + param + "_" + param1, inps)}
                                    <img className={peopleCSS.imginp+" yes "} src={yes} onClick={(e)=>onFin(e, inps, forceUpdate, CHANGE_PARENTS, parentsInfo)} title="Подтвердить" alt=""/>
                                    <img className={peopleCSS.imginp} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                                </div>
                            </div>
                        )}
                        <div className={parentsCSS.upr}>
                            <img className={peopleCSS.imginp} data-id1={param} style={{marginRight: "1vw"}} src={no} onClick={(e)=>onDel(e, CHANGE_PARENTS_DEL_L0)} title="Удалить" alt=""/>
                        </div>
                    </div>
                )}
            </>
        :
            pI.map((param, i, xs, info = parentsInfo[param], ppI = info.par ? Object.getOwnPropertyNames(info.par) : []) =>
                info.par &&
                <div className={peopleCSS.nav_iZag+" "+peopleCSS.nav_iZag1} key={param}>
                    <div className={parentsCSS.uch}>
                        <div className={peopleCSS.nav_i+" "+parentsCSS.nam} id={peopleCSS.nav_i}>
                            Обучающийся: {info.name}
                        </div>
                        {info.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info.login)} title="Перейти в профиль" alt=""/>}
                        <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                            {ppI.length > 1 ? "Представители:" : "Представитель:"}
                        </div>
                    </div>
                    {ppI.map((param1, i1, xs1, info1 = info.par[param1]) =>
                        <div key={param1}>
                            <div className={peopleCSS.nav_i+" "+peopleCSS.nav_iZag2} id={peopleCSS.nav_i}>
                                {info1.name}
                            </div>
                            {info1.login && <img className={peopleCSS.profIm} src={themeState.theme_ch ? profd : profl} onClick={e=>goToProf(info1.login)} title="Перейти в профиль" alt=""/>}
                        </div>
                    )}
                </div>
            )
}

function codPepL1C(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(CHANGE_PARENTS, msg.id, "par", msg.id1, msg.code, "link"));
}

export function codPar (id, title, text) {
    console.log("codPar");
    sendToServer({
        id: id
    }, 'PATCH', cAuth+"setCodePep")
        .then(data => {
            console.log(data);
            if(data.status == 200){
                dispatch(changeEvents(CHANGE_EVENT, undefined, undefined, title, text, 10));
                // dispatch(changePeople(CHANGE_PARENTS_L1, undefined, data.id, undefined, data.body));
            }
        });
}

function addKidC(e) {
    const msg = JSON.parse(e.data);
    dispatch(changePeople(CHANGE_PARENTS_L1, undefined, msg.id, undefined, msg.body));
}

export function addKid (bod, id, par) {
    console.log("addKid");
    sendToServer({
        bod: bod,
        id: id
    }, 'POST', cParents+"addPar")
        .then(data => {
            console.log(data);
            if(data.status == 201){
                // dispatch(changePeople(CHANGE_PARENTS_L1, undefined, data.id, undefined, data.body));
                id = undefined;
                dispatch(changePeople(CHANGE_PARENTS_DEL_L1, "nw", "par"));
                par.setAttribute('data-st', '0');
            }
        });
}

function onCon(e) {
    setInfo();
}

function setParents(firstG, bodyG) {
    selGr = firstG != undefined ? firstG : groupsInfo.els.group;
    sendToServer(0, 'GET', cParents+"getParents/" + selGr)
        .then(data => {
            console.log(data);
            dispatch(changePeople(CHANGE_PARENTS_GL, undefined, undefined, undefined, data.body.bodyP));
            if(firstG != undefined) {
                dispatch(changeGroups(CHANGE_GROUPS_GL, undefined, bodyG));
                dispatch(changeGroups(CHANGE_GROUPS_GR, undefined, firstG));
            }
            if(data.status == 200){
                dispatch(changePeople(CHANGE_CLASSMATES_GL, undefined, undefined, undefined, data.body.bodyC));
                inps.lpI = Object.getOwnPropertyNames(data.body.bodyC);
                for(let i = 0; i < inps.lpI.length; i++){
                    if(data.body.bodyP[inps.lpI[i]]) {
                        inps.lpI.splice(i, 1);
                        i--;
                    }
                }
                if(inps.lpI.length > 0 && !data.body.bodyC[inps.nyid]) {
                    inps.nyid = inps.lpI[0];
                    dispatch(changePeople(CHANGE_PARENTS_L1_PARAM, undefined, "nw", undefined, data.body.bodyC[inps.nyid].name));
                }
            }
        });
}

function setInfo() {
    var url = "getInfo";
    if(cState.role == 3) url = "getInfoFH";
    sendToServer(0, 'GET', cParents + url)
        .then(data => {
            console.log(data);
            if(data.status == 200){
                if(cState.role == 3) {
                    setEvGr(cState, dispatch);
                    setParents(parseInt(data.body.firstG), data.body.bodyG);
                } else {
                    setParents();
                }
            }
        });
}

export function Parents() {
    parentsInfo = useSelector(parents);
    classmatesInfo = useSelector(classmates);
    themeState = useSelector(themes);
    cState = useSelector(states);
    groupsInfo = useSelector(groups);
    if(!dispatch) {
        setActNew(3);
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        eventSource.addEventListener('addKidC', addKidC, false);
        eventSource.addEventListener('codPepL1C', codPepL1C, false);
    }
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Parents.jsx");
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            eventSource.removeEventListener('addKidC', addKidC);
            eventSource.removeEventListener('codPepL1C', codPepL1C);
            console.log("I was triggered during componentWillUnmount Parents.jsx");
        }
    }, []);
    let pI = Object.getOwnPropertyNames(parentsInfo);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(groupsInfo.els.group && selGr != groupsInfo.els.group && eventSource.readyState == EventSource.OPEN){
            setParents();
        }
        console.log('componentDidUpdate Parents.jsx');
    });
    return (
        <div className={peopleCSS.header}>
            <Helmet>
                <title>Родители</title>
            </Helmet>
            {pI.length == 0 ?
                    <ErrFound text={errText}/>
                :
                    <>
                        {(cState.auth && cState.role == 3) &&
                            <div className={parentsCSS.pane}>
                                <Pane cla={true}/>
                            </div>
                        }
                        <div className={peopleCSS.blockPep} style={{marginTop: (cState.auth && cState.role == 3) ? "7vh" : undefined}}>
                            <div className={peopleCSS.pep}>
                                <div className={peopleCSS.nav_iZag}>
                                    <div className={peopleCSS.nav_i} id={peopleCSS.nav_i}>
                                        Родители
                                    </div>
                                    {getParents(pI, (cState.auth && cState.role == 3))}
                                </div>
                            </div>
                        </div>
                    </>
            }
        </div>
    )
}
export default Parents;