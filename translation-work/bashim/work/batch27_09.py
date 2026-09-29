#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 9 (input lines 801-900). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409877: """xxx: played word games with one guy
xxx: in the end it turned out that I'm a sucker, and he studied at the geography faculty""",
409878: """Harsh student weekdays.
- Sunshine, did you fry the cutlets?
- Yes.
- BOTH?!""",
409879: """xxx: Why did vampire movies start being called sagas? Today one girl, looking at the bookshelf, thoughtfully asked whether the Forsytes were vampires?""",
409880: """хxx: and we argued with him
хxx: well, argued...
хxx: he was spouting some bullshit, and I stayed silent and was horrified to myself at how deeply wrong he was""",
409881: """ххх: don't eat off the floor!!
ууу: I'm not squeamish.
ххх: but you seem immortal...""",
409882: """xxx: A programmer friend came over the other day.
xxx: Stared at my embroidery on the wall for a long time, then studied the pattern for 20 minutes, then said "This is the prototype of pixel graphics! It's genius!", grabbed my embroidery magazine and quickly left.
xxx: Now I'm wondering, should I worry or be happy for him?""",
409883: """xxx: The battery in my book died.
xxx: I hate the future.""",
409884: """Он: I'll call you Kisa.
Она: Vorobyaninov?""",
409885: """vdinets@lj:
In almost forty years of zoology I've seen all kinds of things, but still periodically come across something like this...

The crustacean Cymothoa exigua - a fish parasite. Penetrating the fish's mouth through the gills, it attaches to the base of the tongue and with hollow claws drinks blood from the vessels supplying the tongue with blood, until the tongue dries up. After that the parasite attaches to the tongue muscles and replaces the dried-up tongue, fully taking over its functions. The fish, apparently, doesn't experience any particular inconvenience from having its organ replaced by a parasite.

zloradskij: Obviously, a more advanced variety of this parasite doesn't stop in the oral cavity, but crawls up to the brain and takes over its functions.
The hosts don't suffer from this at all, on the contrary, they often reach the highest echelons of power.""",
409886: """XXX: I come to work. And we've each got two roses on the desk. The guys put one each on our desks. And then the boss came and put another one from himself.
XXX: He'd better not have done it at all. The joke is that the roses are absolutely identical. Bought in the same place.""",
409887: """сабж: congratulating female colleagues

а: We're about to congratulate the girls. Set out canapes, poured juice and arranged bouquets. Readiness heightened.
б: just cut the pizza too. everyone ran to the smell, and I'm at the comp
в: Also congratulated ours. All 28 girls. Showered them with flowers, a certificate to "Intimo", decorated the office with balloons and other attributes, fed them pies and juices.
г: We have no women. But the bearded scrawny glasses-wearing admins just in case put on new sweaters. They say there's one woman somewhere, on the next floor.""",
409888: """111: If they'd ring the doorbell quieter when the kid is sleeping - they'd be priceless.
222: For my kid I made a foil contact in the crib, when the kid is inside - the current to the doorbell goes through a resistor, when the crib is empty - the doorbell rings at full power. Pretty simple, considering the kid is in the crib only when sleeping.
333: With natural wetting the garland may come out with special effects. They say one Soviet engineer cured his little one of enuresis this way...""",
409889: """xxx: Wanna feel like a sapper?
xxx: Put a diaper on a sleeping child!""",
409890: """ххх: ahh, youth..
ххх: my windows face the school.
ххх: there's a boy and a girl walking in the yard. the boy really wanted to kiss her
ууу: and she?
ххх: and the girl kept playing hard to get.
ххх: in the end he knocked her into a snowdrift and pelted her with snowballs
ууу: scored!""",
409891: """xxx: I'm the most ordinary!
yyy: All people are unusual
xxx: And I'm not like everyone!""",
409892: """ууу: Future moms! Inviting you to a photoshoot, to participate in a photo project as a model!!! Write in PM.

ххх: I want to be a future mom, does participating in the photoshoot guarantee pregnancy?""",
409893: """Мария: I got a letter today. A real letter, paper. In an envelope with a stamp and all. From Moscow, more precisely, from the glorious city of Lyubertsy, from a friend. And in the letter just two phrases: "Mary! Get on ICQ!\"""",
409894: """xxx: the main thing is not sex but the inner filling.
xxx: me for example I want to be filled with pancakes.""",
409895: """xxx:
We've got one boy here working miracles.

Backstory:
In many schools it's freezing cold, 8 degrees in some, 14 in others. In Taika's school they were allowed to wear outerwear and hats.

So, he called the City Education Department:
- I can't stand onions and garlic, and mom feeds them to me every day so I don't get sick at school. I'm constantly freezing! Tell me, how can I absorb material for straight A's in such conditions, stay an excellent student?!
And at the end:
- I hope my call is recorded?

First, mom was summoned to the City Education Department.
Second, the boy's school shortened the lessons.
Third, in Taika's school too, and in general they didn't study half the week.
Fourth, the boy's teacher said:
- Bogdan, why did you do that? We should've discussed it in class first...
- You'll discuss it with my uncle, who works in the FSB in Moscow, and here I have the right to demand compliance with learning conditions.
Fifth, he's 9 years old.

xxx:
I told Taika all this. She:
- And our school was told the boy complained that his fingers were cold...

xxx:
Taika "is fangirling over him", tomorrow probably the whole school will know this story.
The City Education Department is collecting signatures to make him mayor.""",
409896: """ххх: I don't go on picnics with him anymore, damn medic
ххх: worth alone was his thoughtful phrase last weekend, said in the very midst of the feast
ххх: "Shashlik is just a pork burn\"...""",
409897: """xxx: Is it normal that on our first night, as soon as I undressed, she looked THERE and said: "Fucking nanotechnology!", turned around and left?""",
409898: """xxx: Three rows are sitting. Suddenly one starts converging. The others to it: "Convergence without reason is a sign of d'Alembert\".""",
409899: """xxx: If we suddenly meet gopniks - I tank, and you heal.
ууу: I'd rather res later...""",
409900: """...A student of the famous semiotics professor Yuri Lotman really liked the film "Flights in Dreams and in Reality", and he wrote me a letter - an analysis of the film. It was impossible to read, since he found his own explanation for every step. For example, in the film there's a shot where the main character runs across a field to a haystack. And he's wearing sneakers with red soles. The critic wrote: "These are not just soles - these are red soles. Because the ground is burning under his feet!" And I remember how on set I yelled at the props guy: "Idiot, couldn't you find shoes with normal soles?!"

http://www.ng.ru/saturday/2004-09-10/15_balayn.html""",
409901: """Handur: In first grade we had a living corner (back then it was still allowed). Someone picked up a little jackdaw chick. Still really young, didn't even have proper feathers. Everyone waited for the feathers to grow so we could release it into the wild. And it went and died, the bitch. The vet said it was old age )""",
409902: """xxx: My dream is to teach some stupid and useless subject to the children and grandchildren of my current profs.""",
409903: """xxx: the boss called, delighted: bought 100 licenses for nod32
yyy: congratulations, but your fleet seems half that
xxx: yeah, and for almost a year now almost everything's on ubuntu""",
409904: """XXX: The human brain is the most complex organ and no computer compares to it!
УУУ: moreover, there are frequent cases when people mastered the computer and the internet, but couldn't master the brain""",
409905: """Kolo: Whoever cleans up after the cat, dances him.""",
409906: """Discussion of a camera on the market:
Artem-Programmer: Install Linux, there'll be no problems.
slivva: Uhh... install it on the camera?""",
409907: """xxx:
I once had this prank. Went into the faculty smoking room (I was a student then) and there was a conversation about good and bad TVs. Along the way the conversation sharpened on the topic of color divergence at the screen edges. Someone asked why it happens. And then it hit me...
The main thing was to convince this dude that electrons come in three colors - red, blue and green. Well, I sold it to him, said "You know this without me", he nodded. And then it was easy.
--Wonderful, - I said and poured him another trick about E=mC^2 and E=h&#957;. Twirled these formulas in front of his nose and, like "two times two", proved that blue electrons are the heaviest, green ones a bit lighter, and red ones the lightest. He nodded again.
Then I applied the Lorentz rule for selfish purposes and proved to him that blue electrons deviate less in a magnetic field than red ones, hence the color divergence. Basically, he understood everything. So much so that he tried to sell this crap to the prof at the lab, practically in my own words. The prof had a miserable look, and then stared at one point for a long time. Apparently, picking a fitting diagnosis...""",
409908: """xxxx: At school we once stole a huge chunk of sodium from the chemistry room. For two days we carried it in a little bag, trying to figure out what to do with it. And then it started oxidizing. Then we decided not to mess with it and threw it in the toilet.

xxxx: The second time I saw a wave of such scale, covering all living things, was only 20 years later in the report about the tsunami in Thailand.""",
409909: """xxx: he said he doesn't have a condom
yyy: and what did you say?
xxx: that it's nothing terrible, that I'm already ready to become a mother. and SUDDENLY it turns out he has a condom in his pocket - he just "forgot" about it.""",
409910: """ххх: will you finally take your stupid concert recording? it's lying around, just taking up space on the hard drive
ууу: you short on space or what? you've got a terabyte drive
ххх: yeah, short!
ууу: well delete some crap!
ххх: yeah! and I'LL START WITH YOURS""",
409911: """xxx: Remember how he kept preaching, love, like, can't live without you?
xxx: Well I read him a sermon about how you need to love yourself and live like it's your last day
yyy: That's your thing, yeah
xxx: So this asshole in a week quit smoking, drinking, went to the gym, took care of himself, started reading books. And in a month told me to fuck off, me being so pathetic...""",
409912: """xxx: spring has come
xxx: two-story cats appeared on the streets""",
409913: """A guy with a girl before a pedestrian "zebra".
Д: look how worn out it is, you can barely see it. they drive fast, they won't notice!
П: they'll notice the sign. Besides, if he drives over the zebra, they'll take a grand off him.
Д: yeah, and off me - measurements...""",
409914: """xxx: The most expensive cheese in the world is made not from cow's milk at all - but from moose milk. The cheese costs about 500 dollars per 500 grams

yyy: of course it'll cost 500 dollars, you try milking a MOOSE.""",
409915: """My beloved has an auto repair shop. In the morning I leave for work much earlier than him, he's still sleeping. Kissing him quietly, and he... Pushes me away with the words: "Not me! Take your bumper and get out of here!!!" Such a cutie )""",
409916: """xxx: I cried today )
xxx: here it is the new generation has come
xxx: saw in our yard - kids were jumping over a jump rope
xxx: everything was fine until I saw in the kid's hands at one end of the jump rope a USB, and at the other a miniUSB""",
409917: """my alcoholic friend (16:31:59 7/03/2011)
our copyright prof here asks - how many movies do you download from torrents? everyone at once - what time period do you mean? per week? per month, per year? he - what? you - future copyright specialists, you should answer - what are torrents? what internet? I live in a village, we have no electricity""",
409918: """XXX: who are you drinking with?
YYY: Vitya, Oleg, Yasha and Sanya
XXX: that's who you traded me for
YYY: Didn't trade, just temporarily partially trying to fill the void of your absence
XXX: quiet, let the woman throw a tantrum""",
409919: """Just stopped by a friend. He's got a 15-year-old son, Tyomka, walking around nagging about getting dad to spring for an iPhone.
Father: I'll grab the belt right now and draw you such an iPhone on your ass!
Son: And I'll complain to child protective services and they'll strip you of parental rights!
Father: Well fuck yeah! And they'll give you to an orphanage, and you'll be happy there with an iPhone!""",
409920: """buying a home exercise machine at the sporting goods store.
Seller: Take another barbell plate!
Me: Why?
Seller: It's Maslenitsa after all!""",
409921: """xxx: Oleg is a mega-pessimist!
ххх: there are people who think the glass is half empty - those are pessimists.
ххх: and there's Oleg, he thinks the glass is no longer the same!""",
409922: """Comments on the news about finding proof of extraterrestrial life.
X: I'd be ashamed for humanity if we now met representatives of another civilization. We've got nothing to show but greed and violence, thirst for power and money.
Y: And ballet?""",
409923: """NightSerfer: Greeting card "Thanks for the socks on February 23, darling" - 25 rubles by MasterCard, Apple sticker - 35 rubles by MasterCard, plastic toy phone - 100 rubles by MasterCard, the face of this bitch after unpacking the "present" - priceless""",
409924: """xxx: Our people are so politically savvy that you can still ride and ride on them!""",
409925: """A traffic cop stops you on the eve of March 8. Husband, opening the window:
- Good evening, Mr. Policeman!
Traffic cop, grumbling upset under his nose:
- Gentlemen sit in restaurants, not freeze their asses off on the street on a holiday... Sergeant so-and-so, documents.""",
409926: """oksmih: My husband kissed me in the morning, I got all emotional, my arms went cottony, I dropped my bag with the laptop and broke it""",
409927: """xxx: It'd be cool if you could lose weight on credit.
xxx: Like, lose 15 kg today, and then sit on a diet for a couple months...""",
409928: """xxx: you can be proud of your son )
xxx: 36 different greetings for March 8th )
Мать: received?""",
409929: """From WoW voice chat:
ххх: The day before yesterday I had twins born!
ууу: procced or something ?""",
409930: """Friend: Damn, how incredibly, unbearably I want a woman!
Me: Hmm, well, if it's that urgent - go to a brothel.
Friend: No, I want one I know!
Me: And you go to the brothel more often - you'll get to know people there :D""",
409931: """yesterday lying with my husband, trying to fall asleep, and the neighbors decided to celebrate something. Noise, stomping, impossible to sleep.

я: would you like it if a gravitational anomaly suddenly happened in our house, and we started walking on the ceiling and taking revenge on the neighbors?
он: if a gravitational anomaly suddenly happened in our house, we'd crash from three meters high headfirst into the ceiling, and then the sofa would crush us too!""",
409932: """xxx: Ahaha, we've got another admin here - Ruuusya. Just gave a needy girl a 10k loan till payday.
xxx: SEEMINGLY - everything's fine.
xxx: At 45% interest!!!1111""",
409933: """1: Hmm.... Profound answers , lately , you've got .
2: and you've got extra commas.
1: And ?
2: and spaces.""",
409934: """A letter arrived in the Spam folder:
"From: Gerasim
Subject: Nicotine addiction treatment!"
I think it's unnecessary to ask about the method. Just need to add - 100% guarantee.""",
409935: """xxx: My parents are idiots.
yyy: m?
xxx: Watch my hands: calling from Spain from a Spanish mobile to my Russian mobile, to ask me to google and drop into skype a link to an eggplant caviar recipe.
xxx: I don't even know which part of the phrase to emphasize to accentuate the idiocy.""",
409936: """Serch: A pizza with radius ц and thickness а has volume пи*ц*ц*а.""",
409937: """ххх: just opened the lens case, and there are lenses inside
ххх: first thought: what do I have in my eyes then?""",
409938: """From a dating site.

a girl's profile:
name: alina
height: 172
weight: 55
build: depends on the mood)

supergu: what, are you the Hulk?""",
409939: """xxx: March 8 is the only day of the year when fuel surcharges and pollen tax are included in the price of flowers.""",
409940: """мама: from today we're fasting
me and dad in chorus: and why are you talking about yourself in plural?""",
409941: """Night Kite: Seen the movie "arctic blast"?
_kuchev: No
Night Kite: Totally lol. There's a catastrophe about a cold front freezing people to death in fractions of a second. At a temperature of -38 degrees.""",
409942: """corey: NEVER tease a cat with a sock you're about to put on""",
409943: """xxx: I'm just curious to see who made this ad, to look into his faceted eyes, to find out where his tentacles grow from.
xxx: Because a humanoid wouldn't think of that.""",
409944: """xxx>...when we did the IQ test, several (!) big clients asked to provide a special hidden "top-manager" button.

Because there were cases of objective evaluation of management with subsequent consequences for the tester. Actually, everything's correct - the tester is clearly a fool. And so the button is pressed unnoticed and IQ magically becomes above average, the boss rejoices like a child and everyone's happy. And world peace.""",
409945: """xxx: Was finishing the crossword for mom.
xxx: "Grey matter in the skull"
xxx: Mom's answer: "MINCE".
xxx: Very sweet.""",
409946: """From the discussion of a carpet for 5.458 million dollars.

xxx: I'd take a photo with it""",
409947: """Angelus: My ex-girlfriend faked orgasms so well that honestly, I wanted to stand up and applaud her standing.""",
409948: """dmedvedev: One "planning fallacy" study showed that people with depression most accurately predict project completion deadlines. :-(""",
409949: """<Henry Kane> I'll survive capitalism à la Nosov, but I don't like mail à la Pratchett one bit.""",
409950: """From a forum:
I love tulips and lilies. And my husband always gives roses. I once asked, does he know what my favorite flowers are? he said "tulips and lilies". I ask, then why do you always give roses? answer "well these are roses". I still didn't get it.""",
409951: """Она: can you cook?
Он: Nope, only fried eggs and pancakes. Well, I had to learn. It was embarrassing to kick girls out of my apartment without feeding them.
Она: I hope you threw the fried eggs at their backs when you showed them the door?
Он: yes, and at that time ate pancakes""",
409952: """shaman007: It's just impossible on such a salary: constantly hungry thinks poorly.
nickpo: Constantly hungry thinks excellently. But about the same thing.""",
409953: """cerene: Went to a job interview today. Dressed like a civilian, put makeup on neatly, answered all the idiotic questions competently, and then the psychologist lady asked me to draw a nonexistent animal and name it. Without thinking long I drew a brain slug and signed it "Brainslags" accordingly. She spent half a minute shifting her gaze from me to the drawing and back, and then thoughtfully uttered: "You see, in this test the drawn animal is a representative of the drawer. And how do you position yourself in society?\"""",
409954: """waiting while the boss talks on the phone.

boss: so, are you promoting my site?
boss(dreamily): at least give me hope!
boss: ...
boss: hello, Nadezhda...""",
409955: """<inc> why is your own ICQ number in your "Colleagues" group?
<!> Heh. And where else, not in friends!
<!> After what happened last weekend — he's no friend of mine!""",
409956: """xxx: share your recursion, and it will come back to you more than once""",
409957: """I work as a lawyer. Yesterday represented the interests of mili... a policeman in court. Sued the insurance company that wasn't paying him on KASKO. In general, this policeman gentleman crashed his new "MERSEDES ML AMG", more precisely a bus drove into him. He showed up to court in uniform. The judge (J) starts asking him about the details of the accident.
J: Tell us what happened
P: Well basically I was driving, not bothering anyone and then this bus flew at me..
J: Why so modest? Tell the whole story, in order: "I, an officer of the internal affairs bodies, live within my means, own a new Mercedes-Benz car worth 5.5 million rubles...""",
409958: """KEHb: Saturday, half past 5, last class - economic mathematics.
KEHb: The prof blows up my exhausted brain with one phrase:
KEHb: "...as a result we get something AVERAGE between one and infinity."
KEHb: FUCK, WHAT IS THAT?!""",
409959: """Рыжъ: Age is when you meet a young man's smile in the subway and are just about to bat your eyelashes, when you realize he was smiling not at you, but at your daughter...""",
409960: """anyon: that's nothing. long ago, when I just got the internet and couldn't set it up myself, a young man came to us who for a fee registered the needed ip addresses and dns-server. having done his work, he went to google to check functionality, wrote "по" in the search box, then glanced at 15-year-old me, and with a sour face finished the word "погода\"""",
409961: """FullHouse: Today saw a gopnik squared
FullHouse: Under his tracksuit for warmth he wore a tracksuit""",
409962: """<jein> what do you do for a living?
<dema> I'm a bearded four-eyes in a sweater. Does that tell you anything? ;-)
<jein> geologist? or tourist?
<dema> listen, are you really born in 1987?""",
409963: """Mad Donna:
I recently happened to end up in the company of inveterate housewives: a cosmetics demo, childbirth stories, etc. - all in the best traditions. And then one starts preaching:
- Girls, in no case should you take potatoes with fewer than six eyes - it's a genetically modified product and it 100% causes infertility. And even if you don't get it (!), your children will definitely get infertility (!!), if you eat such potatoes.
And I go and blurt out:
- But what a great way to save on contraception.
They almost beat me up.""",
409964: """a friend of mine delighted me.
я: is your Windows licensed?
он: yes, of course, the keys are pirated, but the distributions are definitely licensed""",
409965: """Doorbell rings. I open (Me) with a cat in my arms. Two boys (M) stand there, offering an election campaign booklet.
М: take a leaflet, please.
Я: Oh, give it, I need it just right.
М: (surprised) REALLY, WHAT FOR?!
Я: the cat's litter box needs changing, it's sturdy
М: then take more from us, please!!!""",
409966: """The system administrator of the city morgue, who mistakenly deleted the entire database for several decades, has been dreaming of Gogol for five days now.""",
409967: """xxx: I decided to stick the modem out the window, because it catches much better there, you know what happened to it?
yyy: nope
xxx: neither do I, it got stolen (""",
409968: """Дмитрий: And maybe the cat was playing and pulled out the cable?
admin: Unlikely. He's been living in the server room for 5 years and walks all over the data center. And during this time NOT A SINGLE incident involving him. Doesn't touch cables, doesn't climb into open system unit coolers, doesn't piss in power supplies. For this he's allowed to walk wherever he wants and sleep on whatever. I trust him more than the cleaning lady.""",
409969: """Ревиль:
How nice it is to fire yourself from this city bustle (almost put an x), get out to your native lands, take a pitchfork in your hands, hug this metal-wooden friend, tell him everything that's accumulated in your soul, until your father's tender cry makes you continue hauling manure""",
409970: """xxx: Jaguar used to have "Jaguar flavoring, identical to natural" in its lineup
xxx: And only now it dawned on me why after overdrinking Yaga in the morning my mouth felt like cats had pissed in it...""",
409971: """xxx: Bought the cat all kinds of squeaky toys, she played them all over the apartment.
xxx: So at night you walk in the dark to the toilet, step on one of these squeakers and realize you don't need the toilet anymore.""",
409972: """dizel: Trolled the little one with the wife. We say "for bad behavior you'll be a week without cartoons". He says: "don't want a week". We: "well then two". Got up to three months.""",
409973: """ihaveabomb: Breaking up by text is dumb and uncool, but still way better than dating by text.""",
409974: """from twitter:

@beav228
Mom went to the store, asked to grab beer and croutons, sitting drinking dark goat with croutons, USSR-style with raisins! XD""",
409975: """Ххх: Yesterday our janitor decided the path near the house was dirty, but didn't want to sweep it and just hosed it down *every entrance has one*. Overnight the water froze. Is he an idiot?
Ххх: The only consolation: you need to fall on your side, since there's a meter-thick layer of snow there.
Ххх: I wonder when the janitor will decide to remove it with a small fire near the standing trees?!""",
409976: """xxx: do you have a microphone?
yyy: no, gave it to a friend to use for a week
yyy: two years ago...""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 801, 900  # 1-based line numbers
    lines = open(src, encoding="utf-8").read().splitlines()
    assert len(lines) >= end, f"only {len(lines)} lines"
    # Build and validate everything BEFORE appending (avoids partial writes).
    out = []
    want_ids = []
    for ln in range(start, end + 1):
        r = json.loads(lines[ln - 1])
        en = EN.get(r["id"])
        assert en is not None, f"missing translation for id {r['id']} (line {ln})"
        assert en, f"empty en for id {r['id']}"
        assert en.count("\n") == r["text"].count("\n"), (
            f"newline mismatch id {r['id']}: en={en.count(chr(10))} ru={r['text'].count(chr(10))}")
        out.append(json.dumps({"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False))
        want_ids.append(r["id"])
    # Resume safety: skip ids already present in dst.
    have = set()
    try:
        with open(dst, encoding="utf-8") as f:
            for l in f:
                l = l.strip()
                if l:
                    have.add(json.loads(l)["id"])
    except FileNotFoundError:
        pass
    new = [o for o, i in zip(out, want_ids) if i not in have]
    with open(dst, "a", encoding="utf-8") as f:
        for o in new:
            f.write(o + "\n")
    print(f"batch lines {start}-{end}: {len(new)} appended "
          f"({len(out) - len(new)} already present)")
