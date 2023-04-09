import React, {useEffect, useReducer, useRef} from "react";
import paneCSS from './pane.module.css';
import {Link} from "react-router-dom";
import {useDispatch, useSelector} from "react-redux";
import {groups, pane, states} from "../../../store/selector";
import {
    CHANGE_EVENTS_STEP,
    CHANGE_PANE,
    CHANGE_PANE_DEL_GRS,
    CHANGE_PANE_GR,
    CHANGE_PANE_GRS,
    changeEvents,
    changeGroups
} from "../../../store/actions";
import PanJs from "./PanJs";
import yes from "../../../media/yes.png";
import no from "../../../media/no.png";
import ed from "../../../media/edit.png";
import {addGroup, chGroup, remGroup} from "../../people/PeopleMain";

let kel, gType;
kel = 0;
gType = {
    true : {
        CHANGE_PANE_GR: "CHANGE_GROUPS_GR",
    },
    false: {
        CHANGE_PANE_GR: "CHANGE_PANE_GR",
    }
};

export function Pane(props) {
    const ele = (x, par) => {
        if(!panJs.inps[par]) panJs.inps[par] = x;
    };
    const getPan = (name, namecl, link, fun) => {
        let cla = [paneCSS.nav_i, paneCSS.nav_iJur, namecl].join(" ");
        return link ?
            <Link className={cla} id={paneCSS.nav_i} to={link} onClick={fun} data-id={namecl} key={namecl} ref={el=>panJs.gr[namecl]=el}>
                {name.nam}
            </Link>
            :
            <div className={cla} id={paneCSS.nav_i} onClick={fun} data-id={namecl} key={namecl} data-st="0" ref={el=>panJs.gr[namecl]=el}>
                {props.cla && cState.role == 3 ?
                    <>
                        <div className={paneCSS.field+" "+paneCSS.fi}>
                            {name}
                        </div>
                        <img className={paneCSS.imgfield+" "+paneCSS.fi} src={ed} onClick={onEdGr} title="Редактировать" alt=""/>
                        <img className={paneCSS.imginp+" "+paneCSS.fi} src={no} onClick={onDel} title="Удалить группу" alt=""/>
                    </> : name
                }
            </div>
    };
    const overpan = () => {
        let wid, el, lst;
        panJs.eles = [];
        for(el of panJs.lGroupY) {
            if(panJs.gr[el].style.display) {
                panJs.gr[el].style.display = "";
            }
        }
        lst = panJs.refes.mor.style.display;
        panJs.refes.mor.style.display = "none";
        if(panJs.refes.lin) {
            panJs.refes.lin.style.display = "none";
        }
        wid = panJs.nav.scrollWidth - panJs.nav.getBoundingClientRect().width;
        if(wid > 1 && panJs.lGroupY.length > 4) {
            let i1 = 3, i = panJs.lGroupY.length-1, el1, el2;
            for(; i > 0; i--) {
                if(wid < 1) {
                    if(i1 < 1) {
                        break;
                    } else i1--;
                }
                el1 = panJs.gr[panJs.lGroupY[i]];
                el2 = panJs.gr1[panJs.lGroupY[i]];
                wid -= el1.getBoundingClientRect().width;
                panJs.eles[panJs.eles.length] = React.cloneElement(el2, {className: el2.props.className+" "+paneCSS.pred});
                el1.style.display = "none";
            }
            panJs.lel = panJs.gr[panJs.lGroupY[i--]];
            panJs.refes.mor.style.display = lst;
            updMor();
        } else {
            panJs.refes.mor.style.display = lst;
        }
        if(panJs.refes.lin) {
            panJs.refes.lin.style.display = "";
        }
    };
    const setGr = (bol) => {
        if(panJs.info.groups[panJs.info.group]){
            if (panJs.act != panJs.info.group || !bol) {
                setActivedMy(panJs.info.group);
            }
        } else {
            if(props.main) {
                setActivedMy(undefined);
                return;
            }
            if(panJs.lGroups.length == 0){
                if(panJs.refes.lin) {
                    panJs.refes.lin.style.width = "0";
                }
                return;
            }
            setActivedMy(panJs.lGroups[0]);
        }
    };
    const updMor = () => {
        if(panJs.eles.length > 0) {
            panJs.refes.lmor = getMore(panJs.eles);
            panJs.parb.updf = true;
            forceUpdate();
            panJs.refes.mor.style.display = "flex";
        }
    };
    const getMore = (el) => {
        let bol = panJs.lel.getBoundingClientRect().width < 50 ? "200%" : "100%";
        panJs.refes.MMel.style.minWidth = bol;
        panJs.refes.MMel.style.marginRight = bol;
        return el.map(par => par);
    };
    const replGr = (x) => {
        let elc, elr, i;
        elc = panJs.gr1[panJs.lel.dataset.id];
        elr = React.cloneElement(elc, {className: elc.props.className+" "+paneCSS.pred});
        for (i = 0; i < panJs.eles.length; i++) {
            if(panJs.eles[i].props["data-id"] == x.dataset.id) {
                panJs.eles[i] = elr;
            }
        }
        panJs.lel.style.display = "none";
        x.style.display = "";
        panJs.lel = x;
        updMor();
    };
    const setActivedMy = (name) => {
        let ao, an;
        ao = panJs.gr[panJs.act];
        an = panJs.gr[name];
        if(ao) ao.dataset.act = '0';
        if(an) {
            panJs.act = name;
            an.dataset.act = '1';
            if(an.style.display == "none") replGr(an);
            if(panJs.refes.lin) {
                panJs.refes.lin.style.left = an.getBoundingClientRect().left+"px";
                panJs.refes.lin.style.width = an.getBoundingClientRect().width+"px";
            }
        }
    };
    const preTim = () => {
        if(!panJs.parb.resiz) {
            panJs.parb.resiz = true;
            panJs.timid = setTimeout(tim,1000);
        }
    };
    const tim = () => {
        if (panJs.parb.resiz) {
            panJs.parb.resiz = false;
            overpan();
            setGr();
        }
    };
    const onEdit = (e) => {
        let par = e.target.parentElement;
        par.dataset.st = '1';
    };
    const onFin = (e) => {
        let par, inp;
        par = e.target.parentElement;
        inp = par.querySelector("input");
        par = par.parentElement;
        if (panJs.inps[inp.id]) {
            if(panJs.edGr){
                if(props.cla) {
                    chGroup(panJs.edGr, inp.value, par);
                } else {
                    dispatch(changeGroups(CHANGE_PANE_GRS, panJs.ke, inp.value, panJs.edGr));
                    par.dataset.st = '0';
                }
                panJs.edGr = undefined;
                panJs.blockCl = false;
            } else {
                if(props.cla) {
                    addGroup(inp.value, par);
                } else {
                    dispatch(changeGroups(CHANGE_PANE_GRS, panJs.ke, inp.value, panJs.lGroups.length == 0 ? 0 : parseInt(panJs.lGroups[panJs.lGroups.length - 1]) + 1));
                    par.dataset.st = '0';
                }
            }
        }
    };
    const onClose = (e) => {
        let par = e.target.parentElement.parentElement;
        par.dataset.st = '0';
        panJs.edGr = undefined;
        panJs.blockCl = false;
    };
    const onEdGr = (e) => {
        let par, inp;
        par = e.target.parentElement;
        panJs.blockCl = "ED";
        panJs.panAdd.dataset.st = '1';
        inp = panJs.panAdd.querySelector("input");
        inp.value = panJs.info.groups[par.dataset.id];
        chStatB({target: inp});
        panJs.edGr = par.dataset.id;
    };
    const onDel = (e) => {
        let par = e.target.parentElement;
        if(props.cla) {
            remGroup(par.dataset.id);
        } else {
            dispatch(changeGroups(CHANGE_PANE_DEL_GRS, panJs.ke, undefined, par.dataset.id));
        }
    };
    const chStatB = (e) => {
        let el = e.target;
        panJs.inps[el.id] = !el.validity.patternMismatch && el.value.length != 0;
        if (panJs.inps[el.id]) {
            el.dataset.mod = '0';
        } else {
            el.dataset.mod = '1';
        }
        el.parentElement.querySelector(".yes").setAttribute("data-enable", +panJs.inps[el.id]);
    };
    const getAdd = (name, namecl) => {
        if(!props.cla || cState.role != 3) return;
        let cla = [paneCSS.nav_i, paneCSS.nav_iZag, paneCSS.nav_iJur].join(" ");
        return (
            <div className={cla} data-st="0" ref={el=>panJs.panAdd = el}>
                <div className={paneCSS.nav_i+" "+paneCSS.chPass} id={paneCSS.nav_i} onClick={onEdit}>
                    {name}
                </div>
                <div className={paneCSS.nav_i+" "+paneCSS.blNew} id={paneCSS.nav_i}>
                    <input className={paneCSS.inp+" "+paneCSS.in} id={"inpt_"} onChange={chStatB} type="text" pattern="^[A-Za-zА-Яа-яЁё\s0-9.-]+$"/>
                    {ele(false, "inpt_")}
                    <img className={paneCSS.imginp+" yes "+paneCSS.in} src={yes} onClick={onFin} title="Подтвердить" alt=""/>
                    <img className={paneCSS.imginp+" "+paneCSS.in} style={{marginRight: "1vw"}} src={no} onClick={onClose} title="Отменить изменения и выйти из режима редактирования" alt=""/>
                </div>
            </div>
        )
    };
    const setGroup = (param) => {
        dispatch(changeGroups(gType[props.cla == true][CHANGE_PANE_GR], panJs.ke, param, undefined, panJs.blockCl));
    };
    const cState = useSelector(states);
    const paneInfo = useSelector(pane);
    const groupsInfo = useSelector(groups);
    const panJs = useRef(new PanJs()).current;
    const dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    const [_, forceUpdate] = useReducer((x) => x + 1, 0);
    if(panJs.info) {
        panJs.lGroups = Object.getOwnPropertyNames(panJs.info.groups);
        panJs.gr1 = {};
        panJs.gr = {};
        panJs.g = panJs.lGroups.map(param =>
            panJs.info.groups[param] &&
            (panJs.gr1[param] = getPan(panJs.info.groups[param], param, panJs.info.groups[param].linke, () => setGroup(param)))
        );
        panJs.lGroupY = Object.getOwnPropertyNames(panJs.gr1);
    }
    useEffect(() => {
        if(panJs.ke == undefined) {
            panJs.pari.paels = 0;
            panJs.ke = kel++;
            if(!props.cla) {
                dispatch(changeGroups(CHANGE_PANE, panJs.ke, props.gro));
                panJs.info = paneInfo.els[panJs.ke];
            } else {
                panJs.nav.style.gridTemplate = "7vh/ 15% auto";
                forceUpdate();
                panJs.info = groupsInfo.els;
            }
        }
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        if(panJs.parb.updf){
            panJs.parb.updf = false;
            console.log('componentDidUpdate onlyRender Pane.jsx');
            return;
        }
        if(panJs.lGroups.length != panJs.pari.paels) {
            panJs.pari.paels = panJs.lGroups.length;
            overpan();
        }
        if(panJs.info) {
            setGr();
        }
        console.log('componentDidUpdate Pane.jsx ke: ' + panJs.ke);
    });
    useEffect(() => {
        dispatch(changeEvents(CHANGE_EVENTS_STEP, 1));
        if(props.cla && cState.role == 3) {
            chStatB({target: panJs.nav.querySelector("." + paneCSS.nav_iZag + " input")});
        }
        console.log("I was triggered during componentDidMount Pane.jsx ke: " + panJs.ke);
        window.addEventListener('resize', preTim);
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_STEP, -1));
            window.removeEventListener('resize', preTim);
            panJs.pari.paels = 0;
            clearTimeout(panJs.timid);
            console.log("I was triggered during componentWillUnmount Pane.jsx ke: " + panJs.ke);
        }
    }, []);
    return (
        <nav className={paneCSS.panel} id="pan" data-mod={props.main ? "1" : "0"} data-ke={panJs.ke} ref={el=>panJs.nav = el}>
            {getAdd("Добавить группу", "Add")}
            {panJs.g}
            <div className={paneCSS.predBlock} ref={re=>panJs.refes.mor = re}>
                <div className={paneCSS.nav_i+' '+paneCSS.nav_iJur+' '+paneCSS.predEl} id={paneCSS.nav_i}>
                    <div className={paneCSS.predInf}>...</div>
                </div>
                <div className={paneCSS.predMenu+" pre "+paneCSS.predMM} ref={re=>panJs.refes.MMel = re}>
                    <div>
                        {panJs.refes.lmor}
                    </div>
                </div>
            </div>
            {!props.main &&
                <div className={paneCSS.lin} ref={ele=>panJs.refes.lin = ele}/>
            }
        </nav>
    )
}
export default Pane;