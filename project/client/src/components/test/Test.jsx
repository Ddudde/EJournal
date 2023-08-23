import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import testCSS from './test.module.css';
import {useDispatch, useSelector} from "react-redux";
import ErrFound from "../other/error/ErrFound";
import {checkbox, states, testRed} from "../../store/selector";
import {CHANGE_EVENTS_CLEAR, CHANGE_TEST_GL, changeCB, changeEvents, changeTest} from "../../store/actions";
import {eventSource, sendToServer, setActived} from "../main/Main";
import CheckBox from "../other/checkBox/CheckBox";

let dispatch, testInfo, inps, cState, errText, checkBoxInfo;
inps = {inpntt : "Текст", inpnzt : "Заголовок", inpndt: new Date().toLocaleString("ru", {day:"2-digit", month: "2-digit", year:"numeric"})};
errText = "Данные для тестирования не сформированы...";

let [_, forceUpdate] = [];

function getPep(x) {
    return <div className={testCSS.infGrid}>
        <div className={testCSS.nav_i+" "+testCSS.nav_iTable}>
            №
        </div>
        <div className={testCSS.nav_i+" "+testCSS.nav_iTable} style={{gridColumn: "2"}}>
            ФИО
        </div>
        <div className={testCSS.nav_i+" "+testCSS.nav_iTable} style={{gridColumn: "3"}}>
            Логин
        </div>
        <div className={testCSS.nav_i+" "+testCSS.nav_iTable} style={{gridColumn: "4"}}>
            Пароль
        </div>
        <div className={testCSS.nav_i+" "+testCSS.nav_iTable} style={{gridColumn: "5"}}>
            Код-приглашение
        </div>
        {Object.getOwnPropertyNames(x).map((param, i) =>
            <>
                <div className={testCSS.nav_i+" "+testCSS.nav_iTable}>
                    {i + 1}
                </div>
                <div className={testCSS.nav_i+" "+testCSS.nav_iTable}>
                    {x[param].fio}
                </div>
                <div className={testCSS.nav_i+" "+testCSS.nav_iTable}>
                    {x[param].login}
                </div>
                <div className={testCSS.nav_i+" "+testCSS.nav_iTable}>
                    {x[param].pass}
                </div>
                <div className={testCSS.nav_i+" "+testCSS.nav_iTable}>
                    {x[param].code}
                </div>
            </>
        )}
    </div>;
}

function chNotif(id) {
    console.log(id, checkBoxInfo[id]);
    sendToServer({
        uuid: cState.uuid,
        id: id,
        val: !checkBoxInfo[id]
    }, 'POST', "test/chBool")
        .then(data => {
            if(data.error == false){
                dispatch(changeTest(CHANGE_TEST_GL, data.bodyT));
            }
        });
}

function setInfo() {
    // dispatch(changeCB("checkbox_debug", true));
    // dispatch(changeCB("checkbox_test", true));
    sendToServer({
        uuid: cState.uuid
    }, 'POST', "test/getInfo")
        .then(data => {
            if(data.error == false){
                for(let id in data.bodyS) {
                    dispatch(changeCB(id, !data.bodyS[id]));
                }
                dispatch(changeTest(CHANGE_TEST_GL, data.bodyT));
            }
        });
}

function onCon(e) {
    setInfo();
}

function visB(e) {
    let el = e.target.nextElementSibling;
    el.dataset.act = el.dataset.act == "0" ? "1" : "0";
}

function ele (x, par) {
    if(!inps[par]) inps[par] = x;
}

export function Test() {
    checkBoxInfo = useSelector(checkbox);
    testInfo = useSelector(testRed);
    cState = useSelector(states);
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Test.jsx");
        setActived(12);
        if(eventSource.readyState == EventSource.OPEN) setInfo();
        eventSource.addEventListener('connect', onCon, false);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            eventSource.removeEventListener('connect', onCon);
            console.log("I was triggered during componentWillUnmount Test.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Test.jsx');
    });
    return <div className={testCSS.header}>
        <Helmet>
            <title>Тестирование</title>
        </Helmet>
        {!Object.getOwnPropertyNames(testInfo).length ?
                <ErrFound text={errText}/>
            :
                <div className={testCSS.block}>
                    <div className={testCSS.inf}>
                        <div className={testCSS.nav_i} id={testCSS.nav_i} onClick={e=>chNotif("checkbox_debug")}>
                            <CheckBox text={"Режим отладки"} checkbox_id={"checkbox_debug"}/>
                        </div>
                        <div className={testCSS.nav_i} id={testCSS.nav_i} onClick={e=>chNotif("checkbox_test")}>
                            <CheckBox text={"Режим тестирования"} checkbox_id={"checkbox_test"}/>
                        </div>
                        <div className={testCSS.blockInfo} data-act={(checkBoxInfo.checkbox_test || false) ? '1' : '0'}>
                            <div className={testCSS.nav_i} id={testCSS.nav_i}>
                                Данные:
                            </div>
                            <div className={testCSS.nav_iZag+" "+testCSS.nav_i}>
                                <div className={testCSS.zag} id={testCSS.nav_i}>
                                    Система
                                </div>
                                <div className={testCSS.razd}>
                                    <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                        Данные администраторов
                                    </div>
                                    <div className={testCSS.blockOtv} data-act="0">
                                        Общие данные аккаунтов администраторов
                                        {getPep(testInfo.admins)}
                                    </div>
                                </div>
                            </div>
                            <div className={testCSS.nav_iZag+" "+testCSS.nav_i}>
                                <div className={testCSS.zag} id={testCSS.nav_i}>
                                    Учебные организации
                                </div>
                                <div className={testCSS.razd}>
                                    {Object.getOwnPropertyNames(testInfo.schools).map((param, i, x, sch = testInfo.schools[param]) =>
                                        <>
                                            <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                                {sch.name}
                                            </div>
                                            <div className={testCSS.blockOtv} data-act="0">
                                                Информация необходимая для тестирования учебных организаций
                                                <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                                    Данные завучей
                                                </div>
                                                <div className={testCSS.blockOtv} data-act="0">
                                                    Общие данные аккаунтов завучей
                                                    {getPep(sch.hteachers)}
                                                </div>
                                                <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                                    Данные педагогов
                                                </div>
                                                <div className={testCSS.blockOtv} data-act="0">
                                                    Общие данные аккаунтов педагогов
                                                    {getPep(sch.teachers)}
                                                </div>
                                                {Object.getOwnPropertyNames(sch.groups).map(param1 =>
                                                    <>
                                                        <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                                            {sch.groups[param1].name}
                                                        </div>
                                                        <div className={testCSS.blockOtv} data-act="0">
                                                            <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                                                Данные учеников
                                                            </div>
                                                            <div className={testCSS.blockOtv} data-act="0">
                                                                Общие данные аккаунтов учеников
                                                                {getPep(sch.groups[param1].kids)}
                                                            </div>
                                                            <div className={testCSS.zag1} id={testCSS.nav_i} onClick={visB}>
                                                                Данные родителей
                                                            </div>
                                                            <div className={testCSS.blockOtv} data-act="0">
                                                                Общие данные аккаунтов родителей
                                                                {getPep(sch.groups[param1].parents)}
                                                            </div>
                                                        </div>
                                                    </>
                                                )}
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
        }
    </div>
}
export default Test;