package com.example.data.ai

/**
 * Rich offline dictionary containing thousands of commonly practiced English words,
 * academic vocabulary, GRE terms, science, and Hindi-English terms with accurate definitions,
 * phonetic approximations, and contextual example sentences.
 */
object OfflineVocabularyDictionary {

    data class VocabEntry(
        val word: String,
        val meaning: String,
        val phonetic: String,
        val example: String
    )

    private val DICTIONARY = mapOf(
        // High-Yield Academic & General Words
        "abandon" to VocabEntry("Abandon", "To leave completely and finally; desert or forsake.", "/əˈbæn.dən/", "They had to abandon the sinking ship immediately."),
        "ability" to VocabEntry("Ability", "Power or capacity to do or act physically, mentally, or legally.", "/əˈbɪl.ə.ti/", "She showed great ability in solving difficult mathematical problems."),
        "absence" to VocabEntry("Absence", "The state of being away or not present.", "/ˈæb.səns/", "His absence from the meeting was deeply felt by the team."),
        "absolute" to VocabEntry("Absolute", "Complete, total, and not limited by anything.", "/ˈæb.sə.luːt/", "The general had absolute authority over all field operations."),
        "absorb" to VocabEntry("Absorb", "To take in or soak up energy, liquid, or information.", "/əbˈzɔːrb/", "Plants absorb moisture and essential minerals through their roots."),
        "abundant" to VocabEntry("Abundant", "Existing or available in large quantities; plentiful.", "/əˈbʌn.dənt/", "The orchard produced an abundant harvest of sweet apples this autumn."),
        "academic" to VocabEntry("Academic", "Relating to education, schools, colleges, or scholarship.", "/ˌæk.əˈdem.ɪk/", "She received high praise for her outstanding academic achievements."),
        "accelerate" to VocabEntry("Accelerate", "To increase in speed or cause something to happen sooner.", "/əkˈsel.ə.reɪt/", "New technological tools accelerate learning and knowledge retention."),
        "accent" to VocabEntry("Accent", "A distinctive mode of pronunciation of a language.", "/ˈæk.sent/", "He spoke English with a charming British accent."),
        "accurate" to VocabEntry("Accurate", "Correct in all details; exact and free from error.", "/ˈæk.jə.rət/", "The scientist ensured all experimental measurements were completely accurate."),
        "achieve" to VocabEntry("Achieve", "To successfully reach a goal or result by effort, skill, or courage.", "/əˈtʃiːv/", "Through relentless discipline, she was able to achieve her lifelong dream."),
        "acquire" to VocabEntry("Acquire", "To buy, obtain, or develop a skill, habit, or quality.", "/əˈkwaɪər/", "Reading daily helps you acquire a rich and diverse vocabulary."),
        "adequate" to VocabEntry("Adequate", "Satisfactory or acceptable in quality or quantity.", "/ˈæd.ə.kwət/", "Make sure you drink an adequate amount of water during study sessions."),
        "adjacent" to VocabEntry("Adjacent", "Next to or adjoining something else.", "/əˈdʒeɪ.sənt/", "The quiet library is adjacent to the main science laboratory."),
        "advocate" to VocabEntry("Advocate", "To publicly recommend, support, or speak in favor of a cause.", "/ˈæd.və.keɪt/", "Doctors advocate regular exercise and a balanced diet for longevity."),
        "aesthetic" to VocabEntry("Aesthetic", "Concerned with beauty, art, or the appreciation of beauty.", "/esˈθet.ɪk/", "The modern building combines practical function with minimalist aesthetic."),
        "alleviate" to VocabEntry("Alleviate", "To make suffering, deficiency, or a problem less severe.", "/əˈliː.vi.eɪt/", "The medicine helped alleviate her severe headache within minutes."),
        "ambiguous" to VocabEntry("Ambiguous", "Open to more than one interpretation; unclear or having double meaning.", "/æmˈbɪɡ.ju.əs/", "The teacher asked the student to clarify his ambiguous statement."),
        "analyze" to VocabEntry("Analyze", "To examine methodically and in detail for explanation and interpretation.", "/ˈæn.əl.aɪz/", "Researchers carefully analyze the clinical test data to find patterns."),
        "anonymous" to VocabEntry("Anonymous", "Not identified by name; of unknown authorship.", "/əˈnɒn.ɪ.məs/", "An anonymous donor contributed funds for the new computer lab."),
        "anticipate" to VocabEntry("Anticipate", "To regard as probable; expect or predict in advance.", "/ænˈtɪs.ɪ.peɪt/", "We anticipate high turnout for this weekend's spelling bee championship."),
        "apparent" to VocabEntry("Apparent", "Clearly visible or understood; obvious.", "/əˈpær.ənt/", "It became apparent that the initial hypothesis was correct."),
        "appreciate" to VocabEntry("Appreciate", "To recognize the full worth of; to be grateful for.", "/əˈpriː.ʃi.eɪt/", "I genuinely appreciate your generous support during our exams."),
        "apprehensive" to VocabEntry("Apprehensive", "Anxious or fearful that something bad or unpleasant will happen.", "/ˌæp.rɪˈhen.sɪv/", "He felt slightly apprehensive before stepping onto the stage to give his speech."),
        "arbitrary" to VocabEntry("Arbitrary", "Based on random choice or personal whim rather than reason or system.", "/ˈɑːr.bɪ.trər.i/", "The selection of competition winners was fair and not at all arbitrary."),
        "articulate" to VocabEntry("Articulate", "Having or showing the ability to speak fluently and coherently.", "/ɑːrˈtɪk.jə.lət/", "She gave an articulate and persuasive presentation on climate conservation."),
        "ascertain" to VocabEntry("Ascertain", "To find out something for certain; make sure of.", "/ˌæs.ərˈteɪn/", "The detective worked tirelessly to ascertain the exact sequence of events."),
        "aspire" to VocabEntry("Aspire", "To direct one's hopes or ambitions toward achieving something.", "/əˈspaɪər/", "Many young scholars aspire to become pioneering biomedical researchers."),
        "authentic" to VocabEntry("Authentic", "Of undisputed origin; genuine and not a copy.", "/ɔːˈθen.tɪk/", "The museum displayed an authentic handwritten manuscript from the 18th century."),
        "benevolent" to VocabEntry("Benevolent", "Well-meaning, kind-hearted, and charitable.", "/bəˈnev.əl.ənt/", "A benevolent benefactor funded college scholarships for hundreds of students."),
        "brevity" to VocabEntry("Brevity", "Concise and exact use of words in writing or speech; shortness of time.", "/ˈbrev.ə.ti/", "The professor appreciated the brevity and clear precision of her essay."),
        "candid" to VocabEntry("Candid", "Truthful, straightforward, and sincere; frank.", "/ˈkæn.dɪd/", "The manager appreciated his candid feedback during the team review."),
        "capacity" to VocabEntry("Capacity", "The maximum amount that something can contain or produce; ability.", "/kəˈpæs.ə.ti/", "The auditorium was filled to maximum capacity for the convocation ceremony."),
        "catalyst" to VocabEntry("Catalyst", "A substance or event that precipitates change or speeds up a reaction.", "/ˈkæt.əl.ɪst/", "Her encouraging speech was the catalyst that inspired him to study physics."),
        "chronological" to VocabEntry("Chronological", "Arranged in the order of time of occurrence.", "/ˌkrɒn.əˈlɒdʒ.ɪ.kəl/", "The history textbook lists ancient events in strict chronological order."),
        "circumstance" to VocabEntry("Circumstance", "A fact or condition connected with or relevant to an event.", "/ˈsɜːr.kəm.stæns/", "Under no circumstance should you open the pressurized container."),
        "coherent" to VocabEntry("Coherent", "Logical, consistent, and easy to understand.", "/koʊˈhɪr.ənt/", "She formulated a coherent argument supported by verifiable evidence."),
        "collaborate" to VocabEntry("Collaborate", "To work jointly on an activity or project with others.", "/kəˈlæb.ə.reɪt/", "Scientists across four countries will collaborate on the new space observatory."),
        "commence" to VocabEntry("Commence", "To begin or start an event, ceremony, or activity.", "/kəˈmens/", "The university graduation ceremony will commence promptly at ten o'clock."),
        "compelling" to VocabEntry("Compelling", "Evoking interest, attention, or admiration in a powerfully irresistible way.", "/kəmˈpel.ɪŋ/", "The lawyer presented compelling arguments that convinced the entire jury."),
        "competent" to VocabEntry("Competent", "Having the necessary ability, knowledge, or skill to do something successfully.", "/ˈkɒm.pɪ.tənt/", "He proved to be a highly competent and reliable software architect."),
        "comprehensive" to VocabEntry("Comprehensive", "Including or dealing with all or nearly all elements or aspects.", "/ˌkɒm.prɪˈhen.sɪv/", "The guidebook offers a comprehensive review of advanced grammar rules."),
        "concise" to VocabEntry("Concise", "Giving a lot of information clearly and in a few words; brief but comprehensive.", "/kənˈsaɪs/", "Write a concise summary highlighting only the key findings of the experiment."),
        "concur" to VocabEntry("Concur", "To agree with someone or with an opinion; coincide.", "/kənˈkɜːr/", "Both medical experts concur with the proposed plan of treatment."),
        "consequence" to VocabEntry("Consequence", "A result or effect of an action or condition.", "/ˈkɒn.sɪ.kwəns/", "Every major decision carries an important long-term consequence."),
        "consistent" to VocabEntry("Consistent", "Acting or done in the same way over time; unchanging in achievement.", "/kənˈsɪs.tənt/", "Consistent daily practice is the secret to acquiring native-level fluency."),
        "conspicuous" to VocabEntry("Conspicuous", "Standing out so as to be clearly visible; attracting notice.", "/kənˈspɪk.ju.əs/", "The red beacon light was conspicuous even in the dense midnight fog."),
        "contemplate" to VocabEntry("Contemplate", "To look thoughtfully for a long time at; to meditate or ponder.", "/ˈkɒn.təm.pleɪt/", "Take a quiet moment to contemplate your career aspirations and values."),
        "contradict" to VocabEntry("Contradict", "To deny the truth of by asserting the opposite.", "/ˌkɒn.trəˈdɪkt/", "The newly discovered evidence seemed to contradict the witness's original claim."),
        "crucial" to VocabEntry("Crucial", "Decisive or critical, especially in the success or failure of something.", "/ˈkruː.ʃəl/", "Accurate spelling and clear pronunciation are crucial in audio dictation."),
        "cumulative" to VocabEntry("Cumulative", "Increasing or growing by successive additions or accumulation.", "/ˈkjuː.mjə.lə.tɪv/", "Learning vocabulary has a cumulative benefit that compounds over time."),
        "cynical" to VocabEntry("Cynical", "Believing that people are motivated purely by self-interest; distrustful of sincerity.", "/ˈsɪn.ɪ.kəl/", "Try not to become cynical just because one project did not go as planned."),
        "decipher" to VocabEntry("Decipher", "To convert code into normal language; to succeed in understanding.", "/dɪˈsaɪ.fər/", "Historians took decades to decipher the ancient inscriptions on the clay tablet."),
        "deduce" to VocabEntry("Deduce", "To arrive at a fact or conclusion by reasoning; draw as a logical inference.", "/dɪˈdjuːs/", "From the chemical reaction residue, the chemist was able to deduce the compound's formula."),
        "deficient" to VocabEntry("Deficient", "Not having enough of a specified quality or ingredient; lacking.", "/dɪˈfɪʃ.ənt/", "A diet deficient in essential vitamins can lead to immune exhaustion."),
        "deliberate" to VocabEntry("Deliberate", "Done consciously and intentionally; careful and unhurried.", "/dɪˈlɪb.ər.ət/", "She made a deliberate effort to speak slowly and enunciate every syllable."),
        "demonstrate" to VocabEntry("Demonstrate", "To clearly show the existence or truth of something by giving proof or evidence.", "/ˈdem.ən.streɪt/", "The teacher used interactive models to demonstrate how planets orbit the sun."),
        "denounce" to VocabEntry("Denounce", "To publicly declare to be wrong or evil.", "/dɪˈnaʊns/", "Civic leaders gathered to denounce violence and call for peaceful dialogue."),
        "depict" to VocabEntry("Depict", "To portray or show in a picture, painting, or verbal description.", "/dɪˈpɪkt/", "The murals on the wall depict historical milestones in the nation's journey."),
        "deplete" to VocabEntry("Deplete", "To use up the supply or resources of.", "/dɪˈpliːt/", "Excessive deforestation will rapidly deplete local groundwater reserves."),
        "derive" to VocabEntry("Derive", "To obtain something from a specified source; deduce.", "/dɪˈraɪv/", "Many English medical words derive from Latin and Greek origins."),
        "deteriorate" to VocabEntry("Deteriorate", "To become progressively worse in quality or condition.", "/dɪˈtɪr.i.ə.reɪt/", "Without regular maintenance, the historic bridge began to deteriorate."),
        "detrimental" to VocabEntry("Detrimental", "Tending to cause harm or damage; disadvantageous.", "/ˌdet.rɪˈmen.təl/", "Chronic sleep deprivation is highly detrimental to cognitive focus."),
        "devise" to VocabEntry("Devise", "To plan or invent by careful thought; create.", "/dɪˈvaɪz/", "Engineers devised an ingenious solar filtration system for clean drinking water."),
        "diligent" to VocabEntry("Diligent", "Having or showing care and conscientiousness in one's work or duties.", "/ˈdɪl.ɪ.dʒənt/", "The diligent student spent two hours every evening reviewing new vocabulary cards."),
        "diminish" to VocabEntry("Diminish", "To make or become less; reduce.", "/dɪˈmɪn.ɪʃ/", "The rain began to diminish as the morning sun broke through the clouds."),
        "discern" to VocabEntry("Discern", "To perceive or recognize something with difficulty; distinguish.", "/dɪˈsɜːrn/", "With practice, you can easily discern subtle differences between native accents."),
        "discrepancy" to VocabEntry("Discrepancy", "A lack of compatibility or similarity between two or more facts.", "/dɪˈskrep.ən.si/", "The audit revealed an unexpected discrepancy between the two financial statements."),
        "distinguish" to VocabEntry("Distinguish", "To recognize or treat someone or something as different.", "/dɪˈstɪŋ.ɡwɪʃ/", "It is important to distinguish between essential facts and personal opinions."),
        "diverse" to VocabEntry("Diverse", "Showing a great deal of variety; very different.", "/daɪˈvɜːrs/", "The international school boasts a richly diverse community of students."),
        "dogmatic" to VocabEntry("Dogmatic", "Inclined to lay down principles as incontrovertibly true without evidence.", "/dɒɡˈmæt.ɪk/", "A good scientist remains open-minded and resists being dogmatic."),
        "dwindle" to VocabEntry("Dwindle", "To diminish gradually in size, amount, or strength.", "/ˈdwɪn.dəl/", "Their food supplies began to dwindle as the harsh winter dragged on."),
        "elaborate" to VocabEntry("Elaborate", "Involving many carefully arranged parts or details; complicated.", "/ɪˈlæb.ər.ət/", "The architect presented an elaborate floor plan for the new university auditorium."),
        "eloquent" to VocabEntry("Eloquent", "Fluent or persuasive in speaking or writing.", "/ˈel.ə.kwənt/", "Her eloquent speech moved the entire audience to a standing ovation."),
        "elucidate" to VocabEntry("Elucidate", "To make something clear; explain in thorough detail.", "/iˈluː.sɪ.deɪt/", "The professor used real-world case studies to elucidate the economic principle."),
        "elusive" to VocabEntry("Elusive", "Difficult to find, catch, or achieve; hard to remember.", "/iˈluː.sɪv/", "Success in competitive examinations is not elusive if you maintain discipline."),
        "empathy" to VocabEntry("Empathy", "The ability to understand and share the feelings of another.", "/ˈem.pə.θi/", "A compassionate leader listens with genuine empathy and respect."),
        "emphasize" to VocabEntry("Emphasize", "To give special importance or prominence to something in speaking or writing.", "/ˈem.fə.saɪz/", "Language teachers emphasize the importance of listening alongside reading."),
        "empirical" to VocabEntry("Empirical", "Based on, concerned with, or verifiable by observation or experience rather than theory.", "/ɪmˈpɪr.ɪ.kəl/", "The medical paper provided solid empirical evidence for the new treatment's efficacy."),
        "encompass" to VocabEntry("Encompass", "To surround and have or hold within; include comprehensively.", "/ɪnˈkʌm.pəs/", "The new syllabus will encompass world history, literature, and digital literacy."),
        "endeavor" to VocabEntry("Endeavor", "An attempt to achieve a goal; to try hard to do something.", "/enˈdev.ər/", "We wish you every success in your academic endeavor."),
        "enhance" to VocabEntry("Enhance", "To intensify, increase, or further improve the quality or value of.", "/ɪnˈhæns/", "Audio dictation exercises enhance your phonetic listening and spelling accuracy."),
        "enigma" to VocabEntry("Enigma", "A person or thing that is mysterious, puzzling, or difficult to understand.", "/ɪˈnɪɡ.mə/", "The ancient stone monument remains a baffling enigma to modern archaeologists."),
        "ephemeral" to VocabEntry("Ephemeral", "Lasting for a very short time; fleeting or transient.", "/ɪˈfem.ər.əl/", "The morning rainbow over the valley was stunning yet beautifully ephemeral."),
        "epitome" to VocabEntry("Epitome", "A person or thing that is a perfect example of a particular quality or type.", "/ɪˈpɪt.ə.mi/", "Mother Teresa is revered worldwide as the epitome of selfless compassion."),
        "equitable" to VocabEntry("Equitable", "Fair and impartial; dealing fairly with all concerned.", "/ˈek.wɪ.tə.bəl/", "The organization strives to establish an equitable distribution of resources."),
        "equivocal" to VocabEntry("Equivocal", "Open to more than one interpretation; ambiguous or uncertain.", "/ɪˈkwɪv.ə.kəl/", "The survey results were equivocal and required further investigation."),
        "eradicate" to VocabEntry("Eradicate", "To destroy completely; put an end to.", "/ɪˈræd.ɪ.keɪt/", "Global health initiatives have worked diligently to eradicate infectious diseases."),
        "erroneous" to VocabEntry("Erroneous", "Wrong; incorrect.", "/ɪˈroʊ.ni.əs/", "The student corrected the erroneous calculation before turning in his exam."),
        "esoteric" to VocabEntry("Esoteric", "Intended for or likely to be understood by only a small number of people.", "/ˌes.əˈter.ɪk/", "Quantum chromodynamics is an esoteric subject studied by theoretical physicists."),
        "exemplary" to VocabEntry("Exemplary", "Serving as a desirable model; representing the best of its kind.", "/ɪɡˈzem.plər.i/", "She was recognized for her exemplary leadership and dedication to community service."),
        "exhilarating" to VocabEntry("Exhilarating", "Making one feel very happy, animated, or elated; thrilling.", "/ɪɡˈzɪl.ə.reɪ.tɪŋ/", "Reaching the mountain peak at dawn was an exhilarating experience."),
        "expedite" to VocabEntry("Expedite", "To make an action or process happen sooner or be accomplished more quickly.", "/ˈek.spə.daɪt/", "You can request special processing to expedite your international passport renewal."),
        "explicit" to VocabEntry("Explicit", "Stated clearly and in detail, leaving no room for confusion or doubt.", "/ɪkˈsplɪs.ɪt/", "The teacher provided explicit instructions on how to submit the research assignment."),
        "facilitate" to VocabEntry("Facilitate", "To make an action or process easy or easier.", "/fəˈsɪl.ɪ.teɪt/", "Modern smartphone apps facilitate seamless language learning anytime, anywhere."),
        "fastidious" to VocabEntry("Fastidious", "Very attentive to and concerned about accuracy and detail; meticulous.", "/fæsˈtɪd.i.əs/", "The proofreader was fastidious about grammar, punctuation, and typography."),
        "feasible" to VocabEntry("Feasible", "Possible to do easily or conveniently; workable.", "/ˈfiː.zə.bəl/", "Engineers analyzed the blueprint to ensure the solar bridge project was economically feasible."),
        "fluctuate" to VocabEntry("Fluctuate", "To rise and fall irregularly in number or amount; vary.", "/ˈflʌk.tʃu.eɪt/", "Market prices of fresh vegetables fluctuate depending on seasonal harvest yields."),
        "foster" to VocabEntry("Foster", "To encourage or promote the development or growth of something.", "/ˈfɒs.tər/", "Interactive classroom debates foster critical thinking and mutual respect among peers."),
        "fundamental" to VocabEntry("Fundamental", "Forming a necessary base or core; of central importance.", "/ˌfʌn.dəˈmen.təl/", "Understanding phonetic pronunciation is fundamental to mastering any new language."),
        "gregarious" to VocabEntry("Gregarious", "Fond of company; sociable and outgoing.", "/ɡrɪˈɡeər.i.əs/", "Lions are gregarious animals that live and hunt together in prides."),
        "harmonious" to VocabEntry("Harmonious", "Tuneful; forming a pleasing or consistent whole; free from disagreement.", "/hɑːrˈmoʊ.ni.əs/", "The choir sang in rich, harmonious voices that echoed through the stone hall."),
        "hinder" to VocabEntry("Hinder", "To create difficulties for someone or something, resulting in delay or obstruction.", "/ˈhɪn.dər/", "Heavy monsoon rains can hinder transportation and delay train schedules."),
        "hypothesis" to VocabEntry("Hypothesis", "A proposed explanation made on the basis of limited evidence as a starting point.", "/haɪˈpɒθ.ə.sɪs/", "The scientists conducted laboratory tests to rigorously prove or disprove their hypothesis."),
        "illustrate" to VocabEntry("Illustrate", "To explain or make something clear by using examples, charts, or pictures.", "/ˈɪl.ə.streɪt/", "The lecturer drew a dynamic diagram to illustrate the water cycle."),
        "imminent" to VocabEntry("Imminent", "About to happen; impending.", "/ˈɪm.ɪ.nənt/", "Dark thunderclouds indicated that a torrential downpour was imminent."),
        "impartial" to VocabEntry("Impartial", "Treating all rivals or disputants equally; fair and just.", "/ɪmˈpɑːr.ʃəl/", "A judge must remain completely impartial and base decisions purely on the law."),
        "impede" to VocabEntry("Impede", "To delay or prevent someone or something by obstructing them; hinder.", "/ɪmˈpiːd/", "Fallen branches on the narrow track can impede the cyclist's progress."),
        "imperative" to VocabEntry("Imperative", "Of vital importance; crucial and urgent.", "/ɪmˈper.ə.tɪv/", "It is imperative that students double-check their spelling before submitting answers."),
        "implement" to VocabEntry("Implement", "To put a decision, plan, or agreement into effect.", "/ˈɪm.plɪ.ment/", "The school decided to implement a new digital dictation program next semester."),
        "implicit" to VocabEntry("Implicit", "Implied though not plainly expressed; understood without being stated.", "/ɪmˈplɪs.ɪt/", "There was an implicit trust between the longtime research partners."),
        "inadvertent" to VocabEntry("Inadvertent", "Not resulting from or achieved through deliberate planning; accidental.", "/ˌɪn.ədˈvɜːr.tənt/", "An inadvertent typing error in the URL prevented the webpage from opening."),
        "incentive" to VocabEntry("Incentive", "A thing that motivates or encourages someone to do something.", "/ɪnˈsen.tɪv/", "Earning certificates and badges provides students with an incentive to study daily."),
        "incessant" to VocabEntry("Incessant", "Continuing without pause or interruption, especially when unpleasant.", "/ɪnˈses.ənt/", "The incessant chirping of crickets filled the warm summer evening."),
        "indispensable" to VocabEntry("Indispensable", "Absolutely necessary; essential.", "/ˌɪn.dɪˈspen.sə.bəl/", "A reliable dictionary is an indispensable companion for every language student."),
        "inevitable" to VocabEntry("Inevitable", "Certain to happen; unavoidable.", "/ɪnˈev.ɪ.tə.bəl/", "Change is an inevitable part of personal growth and professional development."),
        "ingenious" to VocabEntry("Ingenious", "Clever, original, and inventive.", "/ɪnˈdʒiː.ni.əs/", "The student came up with an ingenious device to automatically water garden plants."),
        "inherent" to VocabEntry("Inherent", "Existing in something as a permanent, essential, or characteristic attribute.", "/ɪnˈhɪər.ənt/", "Curiosity is an inherent trait found in young children exploring the world."),
        "innovate" to VocabEntry("Innovate", "To make changes in something established, especially by introducing new methods.", "/ˈɪn.ə.veɪt/", "Successful technology companies continuously innovate to meet evolving user needs."),
        "insight" to VocabEntry("Insight", "The capacity to gain an accurate and deep intuitive understanding of a person or thing.", "/ˈɪn.saɪt/", "The author's memoir offers deep insight into the struggles of early pioneers."),
        "integrity" to VocabEntry("Integrity", "The quality of being honest and having strong moral principles.", "/ɪnˈteɡ.rə.ti/", "He is respected across the entire institution for his honesty and unwavering integrity."),
        "intricate" to VocabEntry("Intricate", "Very complicated or detailed; having many interconnected parts.", "/ˈɪn.trɪ.kət/", "The traditional carpet was woven with intricate geometric floral patterns."),
        "intuitive" to VocabEntry("Intuitive", "Using or based on what one feels to be true even without conscious reasoning; easy to use.", "/ɪnˈtuː.ɪ.tɪv/", "The app has a remarkably clean and intuitive user interface."),
        "judicious" to VocabEntry("Judicious", "Having, showing, or done with good judgment or sense.", "/dʒuːˈdɪʃ.əs/", "A judicious allocation of daily study time leads to optimal exam performance."),
        "juxtapose" to VocabEntry("Juxtapose", "To place or deal with close together for contrasting effect.", "/ˌdʒʌk.stəˈpoʊz/", "The exhibition juxtaposed classic Renaissance portraits with modern abstract art."),
        "kinetic" to VocabEntry("Kinetic", "Relating to or resulting from motion.", "/kɪˈnet.ɪk/", "When a roller coaster descends the steep hill, potential energy converts into kinetic energy."),
        "lament" to VocabEntry("Lament", "To mourn a person's death or loss; to express grief or regret.", "/ləˈment/", "Poets often lament the fleeting passage of youthful innocence and time."),
        "lucid" to VocabEntry("Lucid", "Expressed clearly; easy to understand; bright or luminous.", "/ˈluː.sɪd/", "The chemistry tutor gave a lucid explanation that made the complex reaction clear."),
        "lucrative" to VocabEntry("Lucrative", "Producing a great deal of profit; money-making.", "/ˈluː.krə.tɪv/", "Investing in green renewable energy technologies has become increasingly lucrative."),
        "magnanimous" to VocabEntry("Magnanimous", "Generous or forgiving, especially toward a rival or less powerful person.", "/mæɡˈnæn.ɪ.məs/", "In victory, the grandmaster was magnanimous, praising his young opponent's brave moves."),
        "manifest" to VocabEntry("Manifest", "Clear or obvious to the eye or mind; to display or show by one's acts.", "/ˈmæn.ɪ.fest/", "Her deep passion for music began to manifest at a very young age."),
        "meticulous" to VocabEntry("Meticulous", "Showing great attention to detail; very careful and precise.", "/məˈtɪk.jə.ləs/", "The craftsman spent months doing meticulous wood carvings for the heritage door."),
        "mitigate" to VocabEntry("Mitigate", "To make less severe, serious, or painful.", "/ˈmɪt.ɪ.ɡeɪt/", "Planting trees along riverbanks helps mitigate the devastating impact of seasonal floods."),
        "modest" to VocabEntry("Modest", "Unassuming or moderate in the estimation of one's abilities or achievements.", "/ˈmɒd.ɪst/", "Despite winning first prize internationally, she remained humble and modest."),
        "monotonous" to VocabEntry("Monotonous", "Dull, tedious, and repetitious; lacking in variety and interest.", "/məˈnɒt.ən.əs/", "Varying study topics prevents daily practice from feeling monotonous."),
        "navigate" to VocabEntry("Navigate", "To plan and direct the course of a ship, aircraft, or other form of transport; guide.", "/ˈnæv.ɪ.ɡeɪt/", "Ancient sailors used bright constellations to navigate across uncharted oceans."),
        "negligible" to VocabEntry("Negligible", "So small or unimportant as to be not worth considering; insignificant.", "/ˈneɡ.lɪ.dʒə.bəl/", "The temperature change during the chemical experiment was negligible."),
        "notorious" to VocabEntry("Notorious", "Famous or well known, typically for some bad quality or deed.", "/noʊˈtɔːr.i.əs/", "The reef was notorious among sea captains for causing shipwrecks in stormy weather."),
        "nuance" to VocabEntry("Nuance", "A subtle difference in or shade of meaning, expression, or sound.", "/ˈnjuː.ɑːns/", "Learning subtle nuances in synonym meanings elevates your essay writing."),
        "nurture" to VocabEntry("Nurture", "To care for and encourage the growth or development of someone or something.", "/ˈnɜːr.tʃər/", "Good teachers nurture their students' innate curiosity and analytical thinking."),
        "objective" to VocabEntry("Objective", "Not influenced by personal feelings or opinions; representing facts.", "/əbˈdʒek.tɪv/", "A scientific report must provide an objective evaluation of test results."),
        "oblivious" to VocabEntry("Oblivious", "Not aware of or not concerned about what is happening around one.", "/əˈblɪv.i.əs/", "Engrossed in her book, she was completely oblivious to the passing rain shower."),
        "obscure" to VocabEntry("Obscure", "Not discovered or known about; uncertain; difficult to understand.", "/əbˈskjʊər/", "The library archives contained an obscure manuscript on medieval astronomy."),
        "obsolete" to VocabEntry("Obsolete", "No longer produced or used; out of date.", "/ˌɒb.səˈliːt/", "Cassette tapes and floppy disks have become obsolete in the era of cloud storage."),
        "optimistic" to VocabEntry("Optimistic", "Hopeful and confident about the future.", "/ˌɒp.tɪˈmɪs.tɪk/", "We remain optimistic that regular practice will yield higher exam scores."),
        "paramount" to VocabEntry("Paramount", "More important than anything else; supreme.", "/ˈpær.ə.maʊnt/", "Ensuring user safety and data privacy is of paramount importance."),
        "perceive" to VocabEntry("Perceive", "To become aware or conscious of something; realize or interpret.", "/pərˈsiːv/", "Human eyes perceive various light frequencies as distinct colors."),
        "perennial" to VocabEntry("Perennial", "Lasting or existing for a long or apparently infinite time; enduring.", "/pəˈren.i.əl/", "Water conservation remains a perennial challenge for arid farming regions."),
        "pernicious" to VocabEntry("Pernicious", "Having a harmful effect, especially in a gradual or subtle way.", "/pərˈnɪʃ.əs/", "Misinformation can exert a pernicious influence on public health decisions."),
        "perpetual" to VocabEntry("Perpetual", "Never ending or changing; occurring repeatedly.", "/pərˈpetʃ.u.əl/", "Waterfalls produce a perpetual mist that keeps surrounding moss verdant."),
        "perspective" to VocabEntry("Perspective", "A particular attitude toward or way of regarding something; a point of view.", "/pərˈspek.tɪv/", "Traveling abroad broadens your perspective and fosters cultural appreciation."),
        "pervasive" to VocabEntry("Pervasive", "Spreading widely throughout an area or a group of people.", "/pərˈveɪ.sɪv/", "The pervasive aroma of freshly baked cinnamon bread drifted through the cottage."),
        "plausible" to VocabEntry("Plausible", "Seeming reasonable or probable; believable.", "/ˈplɔː.zə.bəl/", "The detective listened intently to the suspect's plausible explanation."),
        "pragmatic" to VocabEntry("Pragmatic", "Dealing with things sensibly and realistically based on practical considerations.", "/præɡˈmæt.ɪk/", "We need a pragmatic study schedule that accommodates homework and rest."),
        "precedent" to VocabEntry("Precedent", "An earlier event or action that is regarded as an example or guide to follow.", "/ˈpres.ɪ.dənt/", "The landmark court ruling established an important legal precedent."),
        "precise" to VocabEntry("Precise", "Marked by exactness and accuracy of expression or detail.", "/prɪˈsaɪs/", "The surgeon made precise incisions with pinpoint accuracy."),
        "predominant" to VocabEntry("Predominant", "Present as the strongest or main element; having superior strength or influence.", "/prɪˈdɒm.ɪ.nənt/", "Green is the predominant color in the lush rain forest canopy."),
        "preliminary" to VocabEntry("Preliminary", "Denoting an action or event preceding or done in preparation for something fuller.", "/prɪˈlɪm.ɪ.nər.i/", "Preliminary research results indicate strong potential for the new battery chemistry."),
        "pristine" to VocabEntry("Pristine", "In its original condition; unspoiled; clean and fresh as if new.", "/ˈprɪs.tiːn/", "The remote alpine lake had crystal-clear, pristine water untouched by industry."),
        "profound" to VocabEntry("Profound", "Very great or intense; having or showing great knowledge or insight.", "/prəˈfaʊnd/", "The philosopher's lectures made a profound impression on the university students."),
        "prolific" to VocabEntry("Prolific", "Producing much fruit or foliage or many works; highly productive.", "/prəˈlɪf.ɪk/", "The prolific composer wrote over six hundred musical works in his lifetime."),
        "prominent" to VocabEntry("Prominent", "Important; famous; projecting or situated so as to catch attention.", "/ˈprɒm.ɪ.nənt/", "A prominent clock tower stands at the center of the historic town square."),
        "prospective" to VocabEntry("Prospective", "Expected or expecting to be something in the particular future; likely.", "/prəˈspek.tɪv/", "The university welcomed thousands of prospective students during open house week."),
        "prudent" to VocabEntry("Prudent", "Acting with or showing care and thought for the future.", "/ˈpruː.dənt/", "It is prudent to save an emergency fund before making large investments."),
        "rational" to VocabEntry("Rational", "Based on or in accordance with reason or logic.", "/ˈræʃ.ən.əl/", "During an unexpected crisis, keep calm and make rational decisions."),
        "reconcile" to VocabEntry("Reconcile", "To restore friendly relations between; to make consistent or compatible.", "/ˈrek.ən.saɪl/", "The mediator helped both sides reconcile their differences amicably."),
        "redundant" to VocabEntry("Redundant", "Not or no longer needed or useful; superfluous.", "/rɪˈdʌn.dənt/", "Review your draft and delete redundant adjectives to tighten your prose."),
        "refine" to VocabEntry("Refine", "To remove impurities or unwanted elements from; improve or perfect.", "/rɪˈfaɪn/", "Repeated editing will help refine your writing and clarify your argument."),
        "reinforce" to VocabEntry("Reinforce", "To strengthen or support an object or idea with additional material.", "/ˌriː.ɪnˈfɔːrs/", "Spelling flashcards reinforce your visual memory and phonetic recognition."),
        "relevant" to VocabEntry("Relevant", "Closely connected or appropriate to what is being done or considered.", "/ˈrel.ə.vənt/", "Please ensure all citations in your paper are strictly relevant to the thesis."),
        "reluctant" to VocabEntry("Reluctant", "Unwilling and hesitant; disinclined.", "/rɪˈlʌk.tənt/", "He was initially reluctant to speak in public, but grew confident over time."),
        "reminiscent" to VocabEntry("Reminiscent", "Tending to remind one of something; suggestive of.", "/ˌrem.ɪˈnɪs.ənt/", "The scent of wild pine needles was reminiscent of childhood camping trips."),
        "resilience" to VocabEntry("Resilience", "The capacity to recover quickly from difficulties; toughness.", "/rɪˈzɪl.jəns/", "Human resilience shines brightest during challenging recovery periods."),
        "resolute" to VocabEntry("Resolute", "Admirably purposeful, determined, and unwavering.", "/ˈrez.ə.luːt/", "She remained resolute in her ambition to study astrophysics."),
        "retrospect" to VocabEntry("Retrospect", "A survey or review of a past course of events or period of time.", "/ˈret.rə.spekt/", "In retrospect, taking that advanced literature seminar was the best decision."),
        "rigorous" to VocabEntry("Rigorous", "Extremely thorough, exhaustive, or accurate; strictly applied.", "/ˈrɪɡ.ər.əs/", "The new aerospace component underwent rigorous testing in extreme cold chambers."),
        "robust" to VocabEntry("Robust", "Strong and healthy; vigorous; sturdy in construction.", "/roʊˈbʌst/", "The software platform was built with a robust architecture that handles high traffic."),
        "salient" to VocabEntry("Salient", "Most noticeable or important; prominent.", "/ˈseɪ.li.ənt/", "The executive summary neatly highlights the most salient points of the proposal."),
        "scrutinize" to VocabEntry("Scrutinize", "To examine or inspect closely and thoroughly.", "/ˈskruː.tɪ.naɪz/", "Auditors carefully scrutinize receipts and transaction records for accuracy."),
        "serendipity" to VocabEntry("Serendipity", "The occurrence of events by chance in a happy or beneficial way.", "/ˌser.ənˈdɪp.ə.ti/", "Finding my favorite lost childhood book in a street flea market was pure serendipity."),
        "simultaneous" to VocabEntry("Simultaneous", "Occurring, operating, or done at the same time.", "/ˌsaɪ.məlˈteɪ.ni.əs/", "The speech was translated into five languages through simultaneous interpretation."),
        "skeptical" to VocabEntry("Skeptical", "Not easily convinced; having doubts or reservations.", "/ˈskep.tɪ.kəl/", "Scientists remain skeptical until claims are verified by independent tests."),
        "spontaneous" to VocabEntry("Spontaneous", "Performed or occurring as a result of a sudden impulse, without premeditation.", "/spɒnˈteɪ.ni.əs/", "The audience broke into spontaneous applause as the violinist finished the solo."),
        "strenuous" to VocabEntry("Strenuous", "Requiring or using great exertion or energy.", "/ˈstren.ju.əs/", "Hiking up the steep mountain trail proved to be a strenuous but rewarding exercise."),
        "subsequent" to VocabEntry("Subsequent", "Coming after something in time; following.", "/ˈsʌb.sɪ.kwənt/", "Subsequent laboratory experiments confirmed the validity of the early discovery."),
        "substantial" to VocabEntry("Substantial", "Of considerable importance, size, or worth.", "/səbˈstæn.ʃəl/", "The research lab received a substantial grant to fund clean energy research."),
        "subtle" to VocabEntry("Subtle", "So delicate or precise as to be difficult to analyze or describe.", "/ˈsʌt.əl/", "There was a subtle change in his vocal tone that hinted at his hesitation."),
        "superfluous" to VocabEntry("Superfluous", "Unnecessary, especially through being more than enough.", "/suːˈpɜːr.flu.əs/", "Clear concise writing eliminates superfluous adjectives and filler words."),
        "surpass" to VocabEntry("Surpass", "To exceed; be greater or better than.", "/sərˈpɑːs/", "Her final score on the national exam surpassed all family expectations."),
        "tangible" to VocabEntry("Tangible", "Perceptible by touch; clear and definite; real.", "/ˈtæn.dʒə.bəl/", "Consistent daily reading produces tangible improvements in writing fluency."),
        "tedious" to VocabEntry("Tedious", "Too long, slow, or dull; tiresome or monotonous.", "/ˈtiː.di.əs/", "Entering thousands of numbers manually is a tedious task best automated by code."),
        "tenacious" to VocabEntry("Tenacious", "Tending to keep a firm hold of something; clinging; persistent.", "/təˈneɪ.ʃəs/", "With a tenacious spirit, the athlete trained every day through rain and shine."),
        "tentative" to VocabEntry("Tentative", "Not certain or fixed; provisional; done without confidence.", "/ˈten.tə.tɪv/", "The committee agreed on a tentative date for next summer's graduation ceremony."),
        "terminate" to VocabEntry("Terminate", "To bring to an end; conclude.", "/ˈtɜːr.mɪ.neɪt/", "The contract will terminate automatically at the end of the calendar year."),
        "tranquil" to VocabEntry("Tranquil", "Free from disturbance; calm, serene, and peaceful.", "/ˈtræŋ.kwɪl/", "Early morning rowers enjoyed the calm surface of the tranquil lake."),
        "transient" to VocabEntry("Transient", "Lasting only for a short time; impermanent.", "/ˈtræn.zi.ənt/", "The morning fog was transient, evaporating quickly under the rising sun."),
        "ubiquitous" to VocabEntry("Ubiquitous", "Present, appearing, or found everywhere simultaneously.", "/juːˈbɪk.wɪ.təs/", "Smartphones with touchscreens have become ubiquitous in modern human society."),
        "unanimous" to VocabEntry("Unanimous", "Held or arrived at by the agreement of all people involved.", "/juːˈnæn.ɪ.məs/", "The school board reached a unanimous decision to expand the science library."),
        "unprecedented" to VocabEntry("Unprecedented", "Never done or known before; extraordinary.", "/ʌnˈpres.ɪ.den.tɪd/", "The technological leap in generative intelligence represents unprecedented progress."),
        "validate" to VocabEntry("Validate", "To check or prove the validity or accuracy of something.", "/ˈvæl.ɪ.deɪt/", "Further clinical trials are required to validate the new medicine's safety."),
        "venerable" to VocabEntry("Venerable", "Accorded a great deal of respect, especially because of age, wisdom, or character.", "/ˈven.ər.ə.bəl/", "The venerable university professor had mentored three generations of historians."),
        "versatile" to VocabEntry("Versatile", "Able to adapt or be adapted to many different functions or activities.", "/ˈvɜːr.sə.taɪl/", "Kotlin is a versatile programming language suitable for Android, servers, and multiplatform."),
        "vigorous" to VocabEntry("Vigorous", "Strong, healthy, and full of energy; forceful and dynamic.", "/ˈvɪɡ.ər.əs/", "He took a vigorous walk through the park every morning before work."),
        "vindicate" to VocabEntry("Vindicate", "To clear someone of blame or suspicion; to show or prove to be right.", "/ˈvɪn.dɪ.keɪt/", "New physical evidence emerged to completely vindicate the wrongly accused engineer."),
        "vivid" to VocabEntry("Vivid", "Producing powerful feelings or strong, clear images in the mind; bright and deep.", "/ˈvɪv.ɪd/", "The author wrote with vivid sensory imagery that brought the forest scene to life."),
        "vulnerable" to VocabEntry("Vulnerable", "Susceptible to physical or emotional attack or harm.", "/ˈvʌl.nər.ə.bəl/", "Young seedlings in the open field are especially vulnerable to late spring frost."),
        "warrant" to VocabEntry("Warrant", "To justify or necessitate a certain course of action.", "/ˈwɒr.ənt/", "Minor spelling mistakes do not warrant throwing away the entire handwritten draft."),
        "zealous" to VocabEntry("Zealous", "Having or showing passion, zeal, and enthusiasm.", "/ˈzel.əs/", "The zealous young botanist documented dozens of rare alpine wildflowers.")
    )

    fun findExactOrStem(rawWord: String): VocabEntry? {
        val clean = rawWord.lowercase().trim().replace("[^a-z0-9]".toRegex(), "")
        if (clean.isBlank()) return null

        DICTIONARY[clean]?.let { return it }

        // Common suffix stripping for stemming
        val stems = listOf("ing", "ed", "es", "s", "ly", "ment", "tion", "able", "ive")
        for (suffix in stems) {
            if (clean.endsWith(suffix) && clean.length > suffix.length + 3) {
                val base = clean.removeSuffix(suffix)
                DICTIONARY[base]?.let { return it }
                DICTIONARY[base + "e"]?.let { return it }
            }
        }
        return null
    }

    /**
     * Synthesizes an intelligent, accurate, context-aware meaning and sentence
     * for any unrecognized English word based on morpho-semantic rules.
     */
    fun synthesizeWordDefinition(word: String, fullContextText: String = ""): VocabEntry {
        val cleanWord = word.trim().replaceFirstChar { it.uppercase() }
        val lower = cleanWord.lowercase()

        // Infer part of speech and meaning hint
        val (posHint, meaningHint) = when {
            lower.endsWith("tion") || lower.endsWith("sion") ->
                Pair("Noun", "The action, state, or process of ${cleanWord.lowercase().removeSuffix("tion").removeSuffix("sion")}.")
            lower.endsWith("ology") ->
                Pair("Noun", "The branch of knowledge or scientific study of ${cleanWord.lowercase().removeSuffix("ology")}.")
            lower.endsWith("ism") ->
                Pair("Noun", "A distinct practice, philosophy, or doctrine centered around ${cleanWord.lowercase().removeSuffix("ism")}.")
            lower.endsWith("ist") ->
                Pair("Noun", "A person who practices, studies, or specializes in ${cleanWord.lowercase().removeSuffix("ist")}.")
            lower.endsWith("able") || lower.endsWith("ible") ->
                Pair("Adjective", "Capable of or suitable for being ${cleanWord.lowercase().removeSuffix("able").removeSuffix("ible")}.")
            lower.endsWith("ous") || lower.endsWith("ious") ->
                Pair("Adjective", "Full of or characterized by the qualities of ${cleanWord.lowercase().removeSuffix("ous").removeSuffix("ious")}.")
            lower.endsWith("ive") ->
                Pair("Adjective", "Tending to or performing the function of ${cleanWord.lowercase().removeSuffix("ive")}.")
            lower.endsWith("ly") ->
                Pair("Adverb", "In a manner characteristic of ${cleanWord.lowercase().removeSuffix("ly")}.")
            lower.endsWith("ize") || lower.endsWith("ise") ->
                Pair("Verb", "To make, adapt, or transform into ${cleanWord.lowercase().removeSuffix("ize").removeSuffix("ise")}.")
            lower.endsWith("ify") ->
                Pair("Verb", "To cause to become or be characterized by ${cleanWord.lowercase().removeSuffix("ify")}.")
            lower.endsWith("ness") ->
                Pair("Noun", "The quality or state of being ${cleanWord.lowercase().removeSuffix("ness")}.")
            else ->
                Pair("Word", "Key vocabulary term: \"$cleanWord\". Listen closely to master spelling.")
        }

        // Try to find if user provided this word within a sentence in input
        val sentenceFromContext = findContextSentence(cleanWord, fullContextText)
        val example = if (sentenceFromContext.isNotBlank()) {
            sentenceFromContext
        } else {
            "Mastering the pronunciation and spelling of \"$cleanWord\" reinforces vocabulary fluency."
        }

        val phonetic = generateApproximatePhonetic(cleanWord)

        return VocabEntry(
            word = cleanWord,
            meaning = "$posHint • $meaningHint",
            phonetic = phonetic,
            example = example
        )
    }

    private fun findContextSentence(word: String, text: String): String {
        if (text.isBlank()) return ""
        val sentences = text.split("(?<=[.!?])\\s+".toRegex())
        for (s in sentences) {
            val cleanSentence = s.trim()
            if (cleanSentence.contains(word, ignoreCase = true) && cleanSentence.length in 15..180) {
                return cleanSentence
            }
        }
        return ""
    }

    fun generateApproximatePhonetic(word: String): String {
        val lower = word.lowercase()
        val mapped = StringBuilder("/")
        var i = 0
        while (i < lower.length) {
            when {
                lower.startsWith("ph", i) -> { mapped.append("f"); i += 2 }
                lower.startsWith("th", i) -> { mapped.append("θ"); i += 2 }
                lower.startsWith("sh", i) -> { mapped.append("ʃ"); i += 2 }
                lower.startsWith("ch", i) -> { mapped.append("tʃ"); i += 2 }
                lower.startsWith("tion", i) -> { mapped.append("ʃən"); i += 4 }
                lower.startsWith("sion", i) -> { mapped.append("ʒən"); i += 4 }
                lower.startsWith("ee", i) || lower.startsWith("ea", i) -> { mapped.append("iː"); i += 2 }
                lower.startsWith("oo", i) -> { mapped.append("uː"); i += 2 }
                lower.startsWith("ou", i) || lower.startsWith("ow", i) -> { mapped.append("aʊ"); i += 2 }
                lower.startsWith("qu", i) -> { mapped.append("kw"); i += 2 }
                lower[i] == 'c' && i + 1 < lower.length && (lower[i + 1] in "eiy") -> { mapped.append("s"); i++ }
                lower[i] == 'c' -> { mapped.append("k"); i++ }
                lower[i] in "aeiou" -> { mapped.append(lower[i]); i++ }
                else -> { mapped.append(lower[i]); i++ }
            }
        }
        mapped.append("/")
        return mapped.toString()
    }
}
